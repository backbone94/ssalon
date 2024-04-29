package kr.co.ssalon.domain.service;

import kr.co.ssalon.domain.entity.Meeting;
import kr.co.ssalon.domain.entity.Member;
import kr.co.ssalon.domain.entity.MemberMeeting;
import kr.co.ssalon.domain.repository.*;
import kr.co.ssalon.oauth2.CustomOAuth2Member;
import kr.co.ssalon.web.dto.MeetingDTO;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import kr.co.ssalon.web.dto.MeetingSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MemberMeetingRepository memberMeetingRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final MemberMeetingService memberMeetingService;

    // 모임 참가
    @Transactional
    public MeetingDTO joinMoim(CustomOAuth2Member customOAuth2Member, Long moimId) throws BadRequestException {
        String username = customOAuth2Member.getUsername();
        Member currentUser = memberService.findMember(username);

        Meeting meeting = meetingRepository.findById(moimId)
                .orElseThrow(() -> new BadRequestException("해당 모임을 찾을 수 없습니다. ID: " + moimId));

        MemberMeeting memberMeeting = MemberMeeting.createMemberMeeting(currentUser, meeting);

        // member entity 업데이트
        // meeting entity 업데이트
        currentUser.addMemberMeeting(memberMeeting);
        meeting.addMemberMeeting(memberMeeting);

        // memberMeeting entity 업데이트
        memberMeetingRepository.save(memberMeeting);

        // 참가한 모임 정보 반환
        return new MeetingDTO(meeting);
    }

    // 모임 목록 조회
    public Page<Meeting> getMoims(MeetingSearchCondition meetingSearchCondition, Pageable pageable) {
        Page<Meeting> meetings = meetingRepository.searchMoims(meetingSearchCondition, pageable);
        return meetings;
    }

    @Transactional
    public Long createMoim(CustomOAuth2Member customOAuth2Member, MeetingDTO meetingDTO) throws BadRequestException {
        String username = customOAuth2Member.getUsername();
        Member currentUser = memberService.findMember(username);

        Meeting meeting = Meeting.createMeeting(meetingDTO, categoryRepository.getReferenceById(meetingDTO.getCategoryId()), paymentRepository.getReferenceById(meetingDTO.getPaymentId()), memberRepository.getReferenceById(meetingDTO.getCreatorId()), ticketRepository.getReferenceById(meetingDTO.getTicketId()));
        meeting.setMeetingPictureUrls(meetingDTO.getMeetingPictureUrls());
        MemberMeeting memberMeeting = MemberMeeting.createMemberMeeting(currentUser, meeting);

        // member entity 업데이트
        // meeting entity 업데이트
        currentUser.addMemberMeeting(memberMeeting);
        meeting.addMemberMeeting(memberMeeting);

        memberMeetingRepository.save(memberMeeting);

        return meeting.getId();
    }

    public Boolean isParticipant(Long moimId, Member member) throws BadRequestException {
        Meeting meeting = findMeeting(moimId);
        List<MemberMeeting> participants = meeting.getParticipants();

        for (MemberMeeting memberMeeting : participants) {
            if (memberMeeting.getMember().equals(member)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public MeetingDTO getMoim(CustomOAuth2Member customOAuth2Member, Long moimId) throws BadRequestException {
        return new MeetingDTO(findMeeting(moimId));
    }

    @Transactional
    public Meeting updateMoim(CustomOAuth2Member customOAuth2Member, Long moimId, MeetingDTO meetingDTO) throws BadRequestException {
        String username = customOAuth2Member.getUsername();
        Member currentUser = memberService.findMember(username);

        if (!findMeeting(moimId).getCreator().equals(currentUser)) {
            throw new BadRequestException();
        }

        meetingDTO.setId(moimId);
        Meeting meeting = Meeting.createMeeting(meetingDTO, categoryRepository.getReferenceById(meetingDTO.getCategoryId()), paymentRepository.getReferenceById(meetingDTO.getPaymentId()), memberRepository.getReferenceById(meetingDTO.getCreatorId()), ticketRepository.getReferenceById(meetingDTO.getTicketId()));
        meeting.setMeetingPictureUrls(meetingDTO.getMeetingPictureUrls());

        List<MemberMeeting> participants = null;
        for(Long id : meetingDTO.getParticipantIds()) {
            participants.add(memberMeetingService.findMemberMeeting(id));
        }
        meeting.setParticipants(participants);

        return meetingRepository.save(meeting);
    }

    @Transactional
    public Long deleteMoim(CustomOAuth2Member customOAuth2Member, Long moimId) throws  BadRequestException {
        String username = customOAuth2Member.getUsername();
        Member currentUser = memberService.findMember(username);

        Meeting meeting = findMeeting(moimId);
        if (!meeting.getCreator().equals(currentUser)) {
            throw new BadRequestException();
        }

        List<MemberMeeting> participants = meeting.getParticipants();
        for (MemberMeeting memberMeeting : participants) {
            Member roopMember = memberService.findMember(memberMeeting.getMember().getId());
            roopMember.getJoinedMeetings().remove(memberMeeting);
            memberRepository.save(roopMember);
            memberMeetingRepository.deleteById(memberMeeting.getId());
        }
        meetingRepository.deleteById(moimId);

        return moimId;
    }

    @Transactional
    public List<Member> getUsers(CustomOAuth2Member customOAuth2Member, Long moimId) throws BadRequestException {
        Meeting meeting = findMeeting(moimId);
        List<MemberMeeting> participants = meeting.getParticipants();
        List<Member> memberList = new ArrayList<>();

        for (MemberMeeting memberMeeting : participants) {
            Member member = memberMeeting.getMember();
            memberList.add(member);
        }

        return memberList;
    }

    public Meeting findMeeting(Long id) throws BadRequestException {
        Optional<Meeting> findMeeting = meetingRepository.findById(id);
        Meeting meeting = validaitonMeeting(findMeeting);
        return meeting;
    }

    private Meeting validaitonMeeting(Optional<Meeting> meeting) throws BadRequestException {
        if (meeting.isPresent()) {
            return meeting.get();
        }else
            throw new BadRequestException("해당 모임을 찾을 수 없습니다");
    }
}
