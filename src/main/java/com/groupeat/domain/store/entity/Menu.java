package com.groupeat.domain.store.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "menu")
public class Menu extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer basePrice;

    @Column(length = 200)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuOptionGroup> optionGroups = new ArrayList<>();

    public void updateOwnerMenuInfo(
            String name,
            Integer basePrice,
            String description,
            String imageUrl
    ) {
        this.name = name;
        this.basePrice = basePrice;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
