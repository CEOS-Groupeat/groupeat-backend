package com.groupeat.domain.store.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "menu_option")
public class MenuOption extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_group_id", nullable = false)
    private MenuOptionGroup optionGroup;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;
}
