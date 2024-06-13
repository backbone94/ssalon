package kr.co.ssalon.domain.service;

import kr.co.ssalon.domain.dto.MeetingDomainDTO;
import kr.co.ssalon.domain.entity.*;
import kr.co.ssalon.domain.repository.*;
import kr.co.ssalon.oauth2.CustomOAuth2Member;
import kr.co.ssalon.web.controller.annotation.WithCustomMockUser;
import kr.co.ssalon.web.dto.MeetingSearchCondition;
import kr.co.ssalon.web.dto.ParticipantDTO;
import kr.co.ssalon.web.dto.TicketInitResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberMeetingRepository memberMeetingRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RecommendService recommendService;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private MeetingService meetingService;

    @InjectMocks
    private MemberService memberService;

    @InjectMocks
    private MemberMeetingService memberMeetingService;

    @InjectMocks
    private CategoryService categoryService;


    String username = "";
    String email = "";
    String role = "";

    @BeforeEach
    public void getUsernameAndEmailAndRole() {
        // Mock 유저(@WithMockUser)의 username, email, role 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2Member customOAuth2Member = (CustomOAuth2Member) authentication.getPrincipal();

        // username 추출
        username = customOAuth2Member.getUsername();

        // email 추출
        email = customOAuth2Member.getEmail();

        // role 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        role = auth.getAuthority();
    }


    @Test
    @DisplayName("MeetingService.createMoim 메소드 테스트")
    @WithCustomMockUser()
    public void 모임개설() throws Exception {
        //given

        // MemberRepository.findByUsername() stub
        Member member = mock(Member.class);
        when(memberRepository.findByUsername(any())).thenReturn(Optional.of(member));

        // MeetingRepository.save() stub
        Meeting meeting = mock(Meeting.class);
        when(meeting.getId()).thenReturn(1L);
        when(meetingRepository.save(any())).thenReturn(meeting);

        // CategoryRepository.findByName() stub
        Category category = mock(Category.class);
        when(categoryRepository.findByName(any())).thenReturn(Optional.of(category));

        // MemberMeetingRepository.save() stub
        MemberMeeting memberMeeting = mock(MemberMeeting.class);
        when(memberMeetingRepository.save(memberMeeting)).thenReturn(memberMeeting);

        // 티켓 초기 정보 설정 stub
        TicketInitResponseDTO TicketInitResponseDTO = mock(TicketInitResponseDTO.class);
        when(ticketService.initTicket(meeting.getId(), "N")).thenReturn(TicketInitResponseDTO);

        // 모임 정보 임베딩 stub
        doNothing().when(recommendService).updateMoimEmbedding(meeting);

        //when (모임 생성)

        // meetingDomainDTO 생성
        MeetingDomainDTO meetingDomainDTO = MeetingDomainDTO.builder()
                .category("운동")
                .meetingPictureUrls(Arrays.asList("https://ssalon.co.kr/picture1", "https://ssalon.co.kr/picture2"))
                .title("운동을 합시다.")
                .description("운동을 좋아하는 사람들의 모임")
                .location("서울 신림")
                .capacity(8)
                .build();
        Long moimId = meetingService.createMoim(username, meetingDomainDTO);

        //then (생성한 모임에 대한 검증)
        assertThat(moimId).isEqualTo(1L);
    }

    @Test
    @DisplayName("MeetingService.join 메소드 테스트")
    @WithCustomMockUser()
    public void 모임참가() throws Exception {
        //given

        // MemberRepository.findByUsername() stub
        Member member = mock(Member.class);
        when(memberRepository.findByUsername(any())).thenReturn(Optional.of(member));

        // MeetingRepository.findById() stub
        Meeting meeting = mock(Meeting.class);
        when(meeting.getId()).thenReturn(1L);
        when(meetingRepository.findById(any())).thenReturn(Optional.of(meeting));

        // MemberMeetingRepository.existsByMemberIdAndMeetingId() stub
        when(memberMeetingRepository.existsByMemberIdAndMeetingId(any(), any())).thenReturn(false);

        // MemberMeetingRepository.save() stub
        MemberMeeting memberMeeting = mock(MemberMeeting.class);
        when(memberMeeting.getId()).thenReturn(1L);
        when(memberMeetingRepository.save(any())).thenReturn(memberMeeting);

        // when (모임 참가)
        Long joinId = meetingService.join(username, meeting.getId());

        // then (참가한 모임에 대한 검증)
        assertThat(joinId).isEqualTo(1L);
    }

    @Test
    @DisplayName("MeetingService.isParticipant 메소드 테스트")
    @WithCustomMockUser(username = "username", email = "email@email.com", role = "ROLE_USER")
    public void 모임참가자확인() throws Exception {
        //given
        Member member = Member.createMember(username, email, role);
        Meeting meeting = mock(Meeting.class);
        MemberMeeting memberMeeting = mock(MemberMeeting.class);

        Optional<Member> optionalMember = Optional.of(member);
        when(memberRepository.findByUsername(username)).thenReturn(optionalMember);

        when(memberMeeting.getMember()).thenReturn(member);
        when(memberMeeting.getMeeting()).thenReturn(meeting);

        when(meeting.getId()).thenReturn(1L);
        when(meeting.getParticipants()).thenReturn(new ArrayList<>(Arrays.asList(memberMeeting)));
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        //when
        Boolean participantCheck = meetingService.isParticipant(meeting.getId(), member);
        //then
        assertThat(participantCheck).isTrue();
    }


    @Test
    @DisplayName("MeetingService.getMoims 메소드 테스트")
    @WithCustomMockUser(username = "username", email = "email@email.com", role = "ROLE_USER")
    public void 모임목록조회() {
        // given

        Member member = Member.createMember(username, email, role);
        String username = member.getUsername();

        // "운동" 카테고리 Mock 객체 생성
        Category category = mock(Category.class);
        when(category.getName()).thenReturn("운동");

        // "운동" 모임 Mock 객체 생성
        Meeting meeting = mock(Meeting.class);
        when(meeting.getCategory()).thenReturn(category);

        // 모임 목록 필터("운동", "서울특별시") 객체 생성
        MeetingSearchCondition meetingSearchCondition = MeetingSearchCondition.builder()
                .category("운동")
                .build();

        // Pageable 객체 생성
        PageRequest pageable = PageRequest.of(0, 10);

        // "운동", "서울특별시" 모임 Mock 객체에 대한 Page 객체 생성
        Page<Meeting> meetingsPage = new PageImpl<>(Collections.singletonList(meeting));
        when(meetingRepository.searchMoims(meetingSearchCondition, username, pageable)).thenReturn(meetingsPage);

        // when (모임 목록 조회)
        Page<Meeting> result = meetingService.getMoims(meetingSearchCondition, username, pageable);

        // then (조회한 모임에 대한 검증)
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory().getName()).isEqualTo("운동");
    }

    @Test
    @DisplayName("MeetingService.editMoim 메소드 테스트")
    @WithCustomMockUser(username = "username", email = "email@email.com", role = "ROLE_USER")
    public void 모임수정() throws Exception {
        /*
        //given
        Member member = Member.createMember(username, email, role);
        Category category = mock(Category.class);
        Meeting meeting = mock(Meeting.class);

        when(meeting.getId()).thenReturn(1L);
        when(meeting.getCreator()).thenReturn(member);
        when(category.getName()).thenReturn("운동");

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.of(category));

        // MeetingDomainDTO 생성 -> 수정할 DTO
        MeetingDomainDTO mtdo = MeetingDomainDTO.builder()
                .category("운동")
                .meetingPictureUrls(new ArrayList<>(new ArrayList<>(Arrays.asList("http:picture1", "http:picture2"))))
                .title("모임 제목")
                .description("모임 내용")
                .location("서울 신림")
                .capacity(8)
                .meetingDate(LocalDateTime.now()).build();
        //when
        Long moimId = meetingService.editMoim(username, meeting.getId(), mtdo);

        //then
        assertThat(moimId).isEqualTo(meeting.getId());


         */
    }

    @Test
    @DisplayName("MeetingService.deleteMoim 메소드 테스트")
    @WithCustomMockUser(username = "username", email = "email@email.com", role = "ROLE_USER")
    public void 모임삭제() throws Exception {
        //given
        Member member = Member.createMember(username, email, role);
        Meeting meeting = mock(Meeting.class);
        when(meeting.getId()).thenReturn(1L);
        when(meeting.getCreator()).thenReturn(member);

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        doNothing().when(memberMeetingRepository).deleteByMeetingId(1L);
        doNothing().when(meetingRepository).deleteById(1L);
        //when
        Long moimId = meetingService.deleteMoim(username, meeting.getId());
        //then

        assertThat(moimId).isEqualTo(meeting.getId());
    }

    @Test
    @DisplayName("MeetingService.getUsers 메소드 테스트")
    @WithCustomMockUser(username = "username", email = "email@email.com", role = "ROLE_USER")
    public void 모임참여유저조회() throws Exception {
        //given
        Meeting meeting = mock(Meeting.class);
        MemberMeeting createMemberMeeting1 = mock(MemberMeeting.class);
        MemberMeeting createMemberMeeting2 = mock(MemberMeeting.class);
        MemberMeeting createMemberMeeting3 = mock(MemberMeeting.class);
        Member createMember1 = mock(Member.class);
        Member createMember2 = mock(Member.class);
        Member createMember3 = mock(Member.class);

        when(createMemberMeeting1.getMember()).thenReturn(createMember1);
        when(createMemberMeeting2.getMember()).thenReturn(createMember2);
        when(createMemberMeeting3.getMember()).thenReturn(createMember3);

        when(createMemberMeeting1.getMember().getNickname()).thenReturn("nickname1");
        when(createMemberMeeting1.getMeeting()).thenReturn(meeting);


        when(createMemberMeeting2.getMember().getNickname()).thenReturn("nickname2");
        when(createMemberMeeting2.getId()).thenReturn(2L);


        when(createMemberMeeting3.getMember().getNickname()).thenReturn("nickname3");
        when(createMemberMeeting3.getId()).thenReturn(3L);

        when(meeting.getId()).thenReturn(1L);
        when(meeting.getParticipants()).thenReturn(new ArrayList<>(Arrays.asList(createMemberMeeting1, createMemberMeeting2, createMemberMeeting3)));
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        //when
        List<ParticipantDTO> participantUsers = meetingService.getUsers(meeting.getId());
        //then
        assertThat(participantUsers.size()).isEqualTo(3);
        assertThat(participantUsers.get(0).getNickname()).isEqualTo(createMemberMeeting1.getMember().getNickname());
        assertThat(participantUsers.get(1).getNickname()).isEqualTo(createMemberMeeting2.getMember().getNickname());
        assertThat(participantUsers.get(2).getNickname()).isEqualTo(createMemberMeeting3.getMember().getNickname());

    }


}
