import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';
import { Trend, Counter, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const KEYWORD = '검색실험';
const SIZE = 20;
const EXPECTED_TOTAL = 2000;

const URL =
    `${BASE_URL}/api/search/stores` +
    `?keyword=${encodeURIComponent(KEYWORD)}` +
    `&sortType=NONE` +
    `&size=${SIZE}` +
    `&pickupDate=2026-10-08` +
    `&pickupTimes=${encodeURIComponent('12:00:00')}` +
    `&quantity=50`;

// 시작 후 60~180초 구간만 본 측정으로 집계한다.
const windows = [
    { from: 60, to: 180, vus: 100 },
];

const metrics = {};

for (const { vus } of windows) {
    metrics[vus] = {
        duration: new Trend(`search_${vus}vu_duration_ms`, true),
        requests: new Counter(`search_${vus}vu_requests`),
        invalid: new Rate(`search_${vus}vu_invalid_rate`),
    };
}

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
        'avg',
        'min',
        'med',
        'p(90)',
        'p(95)',
        'p(99)',
        'max',
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
            name: 'GET /api/search/stores first-page',
            phase,
        },
    });
}

function isValid(res) {
    if (res.status !== 200) {
        return false;
    }

    try {
        const body = res.json();
        const data = body.data;
        const stores = data?.storeList;

        return body.isSuccess === true
            && data.totalElements === EXPECTED_TOTAL
            && Array.isArray(stores)
            && stores.length === SIZE
            && new Set(stores.map((s) => s.storeId)).size === SIZE
            && stores.every((s) =>
                Number.isInteger(s.storeId)
                && typeof s.name === 'string'
                && s.name.includes(KEYWORD)
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
            `전체 ${EXPECTED_TOTAL}개, 첫 페이지 ${SIZE}개, ` +
            '중복 없는 가게 목록과 다음 페이지 커서를 확인해주세요.'
        );
    }

    console.log(JSON.stringify({
        startedAt: new Date().toISOString(),
        url: URL,
        expectedTotal: EXPECTED_TOTAL,
        pageSize: SIZE,
        sleepSeconds: 0.2,
        measurementWindows: windows,
    }));
}

export default function () {
    // 요청 시작 시점을 기준으로 본 측정 구간을 판별한다.
    const elapsed =
        (Date.now() - exec.scenario.startTime) / 1000;

    const measurementWindow = windows.find(
        (w) => elapsed >= w.from && elapsed < w.to
    );

    const phase = measurementWindow
        ? `hold_${measurementWindow.vus}`
        : 'transition';

    const res = requestSearch(phase);
    const valid = isValid(res);

    check(res, {
        'HTTP 200 및 첫 페이지 응답 정상': () => valid,
    });

    if (measurementWindow) {
        const metric = metrics[measurementWindow.vus];

        metric.duration.add(res.timings.duration);
        metric.requests.add(1);
        metric.invalid.add(!valid);
    }

    sleep(0.2);
}