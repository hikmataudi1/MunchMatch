package net.tecfrac.restoapp.entity;

import jakarta.persistence.*;
import lombok.*;
import net.tecfrac.restoapp.enums.Category;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "options")
public class OptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "option_images", joinColumns = @JoinColumn(name = "option_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @CreationTimestamp
    private Date createdAt;


    @ElementCollection
    @CollectionTable(name = "option_tags", joinColumns = @JoinColumn(name = "option_id"))
    @Column(name = "tag")
    private List<String> tags;
}
