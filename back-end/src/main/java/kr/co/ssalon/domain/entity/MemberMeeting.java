package kr.co.ssalon.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class MemberMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_meeting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "qr_link_id")
    private QrLink qrLink;

    @OneToMany(mappedBy = "memberMeeting")
    private final List<Message> messages = new ArrayList<>();

    protected MemberMeeting() {}

    public static MemberMeeting createMemberMeeting(Member member, Meeting meeting) {
        return MemberMeeting.builder()
                .member(member)
                .meeting(meeting)
                .build();
    }
}
