package kr.co.ssalon.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.coyote.BadRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MemberMeeting> participants = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "meeting_picture", joinColumns = @JoinColumn(name = "meeting_id"))
    private final List<String> meetingPictureUrls = new ArrayList<>();


    private String title;
    private String description;
    private String location;
    private Integer capacity;
    private LocalDateTime meetingDate;

    private Boolean isSharable;
    private Boolean isFinished = false;

    private String backgroundColor;

    protected Meeting() {
    }


    // ***** 필드 메서드 *****
    public void changeTitle(String title) {
        this.title = title != null ? title : this.title;
    }

    public void changeDescription(String description) {
        this.description = description != null ? description : this.description;
    }

    public void changeLocation(String location) {
        this.location = location != null ? location : this.location;
    }

    public void changeCapacity(Integer capacity) {
        this.capacity = capacity != null ? capacity : this.capacity;
    }

    public void changeLocalDateTime(LocalDateTime meetingDate) {
        this.meetingDate = meetingDate != null ? meetingDate : this.meetingDate;
    }

    public void changeIsFinished() { this.isFinished = true; }


    public void addMeetingPictureUrls(List<String> meetingPictureUrls) {
        for (String meetingPictureUrl : meetingPictureUrls) {
            getMeetingPictureUrls().add(meetingPictureUrl);
        }
    }

    public void updateMeeting(Category category, List<String> meetingPictureUrls, String title, String description, String location, Integer capacity, LocalDateTime meetingDates) {
        changeCategory(category);
        addMeetingPictureUrls(meetingPictureUrls);
        changeTitle(title);
        changeDescription(description);
        changeLocation(location);
        changeCapacity(capacity);
        changeLocalDateTime(meetingDates);

    }

    // ***** 연관 메서드 *****
    public void ownerMember(Member member) {
        this.creator = member;
    }

    public void changeTicket(Ticket ticket) {
        this.ticket = ticket;
        ticket.changeMeeting(this);
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    public void changePayment(Payment payment) {
        this.payment = payment;
    }

    public void addParticipants(MemberMeeting memberMeeting) throws BadRequestException {
        if(capacity == getParticipants().size()){
            throw new BadRequestException("인원이 다 찼습니다.");
        }
        getParticipants().add(memberMeeting);
    }

    public static Meeting createMeeting(
            Category category,
            Payment payment,
            Member creator,
            List<String> meetingPictureUrls,
            String title,
            String description,
            String location,
            Integer capacity,
            LocalDateTime meetingDate,
            Boolean isSharable
    ) {
        Meeting meeting = Meeting.builder()
                .title(title)
                .description(description)
                .location(location)
                .capacity(capacity)
                .meetingDate(meetingDate)
                .isSharable(isSharable)
                .isFinished(false)
                .backgroundColor("#808080")
                .build();

        meeting.ownerMember(creator);
        meeting.addMeetingPictureUrls(meetingPictureUrls);
        meeting.changeCategory(category);
        meeting.changePayment(payment);
        return meeting;
    }

    public void setParticipants(List<MemberMeeting> participants) {
        this.participants.addAll(participants);
    }

    public void deleteMemberMeeting(MemberMeeting... memberMeeting) {
        getParticipants().removeAll(Arrays.asList(memberMeeting));
    }
}
