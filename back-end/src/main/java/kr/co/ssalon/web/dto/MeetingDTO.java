package kr.co.ssalon.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.ssalon.domain.entity.Meeting;
import kr.co.ssalon.domain.entity.MemberMeeting;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDTO {

    @NotNull
    private Long id;
    @NotBlank
    private Long categoryId;

    private Long paymentId;
    @NotBlank
    private Long creatorId;
    @NotBlank
    private Long ticketId;
    private List<Long> participantIds;

    @NotNull
    private List<String> meetingPictureUrls;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String location;
    @NotBlank
    @Min(1)
    private Integer capacity;
    @NotBlank
    private LocalDateTime meetingDate;

    public MeetingDTO(Meeting meeting) {
        this.id = meeting.getId();
        this.categoryId = meeting.getCategory().getId();
        this.paymentId = meeting.getPayment() != null ? meeting.getPayment().getId() : null;
        this.creatorId = meeting.getCreator() != null ? meeting.getCreator().getId() : null;
        this.ticketId = meeting.getTicket() != null ? meeting.getTicket().getId() : null;
        this.participantIds = meeting.getParticipants().stream().map(MemberMeeting::getId).collect(Collectors.toList());
        this.meetingPictureUrls = meeting.getMeetingPictureUrls();
        this.title = meeting.getTitle();
        this.description = meeting.getDescription();
        this.location = meeting.getLocation();
        this.capacity = meeting.getCapacity();
        this.meetingDate = meeting.getMeetingDate();
    }
}
