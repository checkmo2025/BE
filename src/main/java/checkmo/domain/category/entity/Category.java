package checkmo.domain.category.entity;

import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<MemberCategory> memberCategories;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ClubCategory> clubCategories;
}
