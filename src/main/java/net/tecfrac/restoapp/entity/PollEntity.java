package net.tecfrac.restoapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "polls")
public class PollEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private UserEntity createdBy;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date endDate;

    @Column(nullable = false,name = "is_active")
    private boolean active;

    @Column(nullable = false)
    private boolean allowMultipleVotes;

    @OneToMany(mappedBy = "pollEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PollOptionEntity> pollOptions;


    @ManyToMany
    @JoinTable(
            name = "poll_visible_users",
            joinColumns = @JoinColumn(name = "poll_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> visibleToUsers;
}
