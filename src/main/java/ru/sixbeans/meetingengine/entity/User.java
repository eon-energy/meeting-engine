package ru.sixbeans.meetingengine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sub;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Lob
    private byte[] avatar;

    @Column(columnDefinition = "DATE")
    private LocalDate memberSince;

    @Column(nullable = false)
    private Boolean profileCompleted;

    @Size(max = 3000)
    private String profileDescription;

    @OneToOne(cascade = CascadeType.ALL)
    private UserContacts userContacts;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Tag> tags = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> friends = new HashSet<>();

    public void addTag(Tag tag) {
        tag.getUsers().add(this);
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tag.getUsers().remove(this);
        tags.remove(tag);
    }

    public void addFriend(User user) {
        user.friends.add(this);
        friends.add(user);
    }

    public void removeFriend(User user) {
        user.friends.remove(this);
        friends.remove(user);
    }
}
