import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';
import { Trend, Counter, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const KEYWORD = '띄어쓰기실험 연남 브런치';
const SIZE = 20;
const EXPECTED_TOTAL = 10000;

// 시작 후 60~180초 구간만 본 측정으로 집계
const MEASURE_FROM = 60;
const MEASURE_TO = 180;

const duration = new Trend('search_100vu_duration_ms', true);
const requests = new Counter('search_100vu_requests');
const invalid = new Rate('search_100vu_invalid_rate');

const URL =
    `${BASE_URL}/api/search/stores` +
    `?keyword=${encodeURIComponent(KEYWORD)}` +
    '&sortType=NONE' +
    `&size=${SIZE}`;

export const options = {
    scenarios: {
        search: {
            executor: 'ramping-vus',
            startVUs: 100,
            stages: [
                { duration: '1m', target: 100 },  // 준비
                { duration: '2m', target: 100 },  // 본 측정
                { duration: '15s', target: 0 },   // 종료
            ],
            gracefulRampDown: '10s',
            gracefulStop: '10s',
        },
    },

    maxRedirects: 0,

    summaryTrendStats: [
        'avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max',
    ],

    thresholds: {
        checks: ['rate==1'],
        search_100vu_invalid_rate: ['rate==0'],
    },
};

function requestSearch(phase) {
    return http.get(URL, {
        headers: {
            Accept: 'application/json',
        },
        timeout: '5s',
        tags: {
            name: 'GET /api/search/stores whitespace',
            phase,
        },
    });
}

function isValid(res) {
    if (res.status !== 200) return false;

    try {
        const body = res.json();
        const data = body.data;
        const stores = data?.storeList;

        return body.isSuccess === true
            && data.totalElements === EXPECTED_TOTAL
            && Array.isArray(stores)
            && stores.length === SIZE
            && new Set(stores.map((store) => store.storeId)).size === SIZE
            && stores.every((store) =>
                Number.isInteger(store.storeId)
                && typeof store.name === 'string'
                && store.name.includes(KEYWORD)
            )
            && data.hasNext === true
            && typeof data.nextCursor === 'string'
            && data.nextCursor.length > 0;
    } catch (_) {
        return false;
    }
}

export function setup() {
    const res = requestSearch('preflight');

    if (!isValid(res)) {
        throw new Error(
            `사전 확인 실패: HTTP=${res.status}. ` +
            `전체 ${EXPECTED_TOTAL}개, 첫 페이지 ${SIZE}개인지 확인해주세요. ` +
            `응답: ${String(res.body).slice(0, 1000)}`
        );
    }

    console.log(JSON.stringify({
        startedAt: new Date().toISOString(),
        url: URL,
        expectedTotal: EXPECTED_TOTAL,
        pageSize: SIZE,
        vus: 100,
        sleepSeconds: 0.2,
        measurementWindow: {
            from: MEASURE_FROM,
            to: MEASURE_TO,
            seconds: MEASURE_TO - MEASURE_FROM,
        },
    }));
}

export default function () {
    // 요청 시작 시점으로 본 측정 구간 여부 판단
    const elapsed =
        (Date.now() - exec.scenario.startTime) / 1000;

    const measuring =
        elapsed >= MEASURE_FROM && elapsed < MEASURE_TO;

    const res = requestSearch(
        measuring ? 'measurement' : 'transition'
    );

    const valid = isValid(res);

    check(res, {
        'HTTP 200 및 첫 페이지 응답 정상': () => valid,
    });

    if (measuring) {
        duration.add(res.timings.duration);
        requests.add(1);
        invalid.add(!valid);
    }

    sleep(0.2);
}