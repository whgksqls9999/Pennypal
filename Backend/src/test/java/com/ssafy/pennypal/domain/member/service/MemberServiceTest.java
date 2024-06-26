package com.ssafy.pennypal.domain.member.service;

import com.ssafy.pennypal.domain.member.dto.request.MemberLoginRequest;
import com.ssafy.pennypal.domain.member.dto.request.MemberSignupRequest;
import com.ssafy.pennypal.domain.member.dto.request.MemberUpdateNicknameRequest;
import com.ssafy.pennypal.domain.member.dto.request.MemberUpdatePasswordRequest;
import com.ssafy.pennypal.domain.member.dto.response.MemberLoginResponse;
import com.ssafy.pennypal.domain.member.dto.response.MemberSignupResponse;
import com.ssafy.pennypal.domain.member.entity.Member;
import com.ssafy.pennypal.domain.member.repository.IMemberRepository;
import com.ssafy.pennypal.global.common.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    IMemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("이메일,PW,닉네임,생년월일,이름을 입력 받고, PW는 암호화하여 Member의 정보를 DB에 저장한다.")
    @Test
    void signUp() {
        // given
        MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        // when
        memberService.signUp(memberSignupRequest);
        
        // then
        assertThat(memberRepository.findByMemberEmail("Jake95@naver.com").getMemberNickname()).isEqualTo(Member.builder()
                .memberEmail(memberSignupRequest.getMemberEmail())
                .memberNickname(memberSignupRequest.getMemberNickname())
                .memberPassword(passwordEncoder.encode(memberSignupRequest.getMemberPassword()))
                .memberBirthDate(memberSignupRequest.getMemberBirthDate())
                .memberName(memberSignupRequest.getMemberName())
                .build().getMemberNickname());
    }

    @DisplayName("회원 정보 중 이메일 중복을 검사하여 중복 될 경우 예외 처리")
    @Test
    void isEmailExist() {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        MemberSignupRequest member2 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("섭섭")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("김준섭")
                .build();

        memberService.signUp(member1);
        // when
        MemberSignupResponse memberSignupResponse = memberService.signUp(member2);

        // then
//        assertThat(memberSignupResponse).isEqualTo(MemberSignupResponse.builder()
//                .status(HttpStatus.BAD_REQUEST)
//                .message("이미 사용 중인 이메일 입니다.")
//                .build());
        assertThat(memberSignupResponse.getMessage()).isEqualTo("이미 사용 중인 이메일 입니다.");

    }

    @DisplayName("회원 정보 중 닉네임 중복을 검사하여 중복 될 경우 예외 처리")
    @Test
    void isNicknameExist() {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Taek95@naver.com")
                .memberNickname("SCM")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("오유택")
                .build();

        MemberSignupRequest member2 = MemberSignupRequest.builder()
                .memberEmail("Jake96@naver.com")
                .memberNickname("SCM")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("김준섭")
                .build();

        memberService.signUp(member1);
        // when
        MemberSignupResponse memberSignupResponse = memberService.signUp(member2);

        // then
//        assertThat(memberSignupResponse).isEqualTo(MemberSignupResponse.builder()
//                .status(HttpStatus.BAD_REQUEST)
//                .message("이미 사용 중인 닉네임 입니다.")
//                .build());
        assertThat(memberSignupResponse.getMessage()).isEqualTo("이미 사용 중인 닉네임 입니다.");
    }

    @DisplayName("Id와 패스워드를 받아 로그인을 한다.")
    @Test
    public void login() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("qwer1234")
                .build();

        // when
        Member byMemberEmail = memberRepository.findByMemberEmail(request.getMemberEmail());

        // then
        assertThat(passwordEncoder.matches(request.getMemberPassword(), byMemberEmail.getMemberPassword())).isTrue();
    }

    @DisplayName("로그인 했을 때 존재 하지 않는 email이면 예외메시지와 Status를 반환한다.")
    @Test
    public void loginEmailNotExist() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Toni95@naver.com")
                .memberPassword("qwer1234")
                .build();

        // when
        ApiResponse<MemberLoginResponse> login = memberService.login(request);

        // then
        assertThat(login.getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(login.getMessage())
                .isEqualTo("존재하지 않는 ID 입니다.");

    }

    @DisplayName("로그인 했을 때 비밀번호가 틀리면 예외메시지와 Status를 반환한다.")
    @Test
    public void loginWrongPassword() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("1234qwer")
                .build();

        // when
        ApiResponse<MemberLoginResponse> login = memberService.login(request);

        // then
        assertThat(login.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(login.getMessage())
                .isEqualTo("비밀 번호를 다시 입력해주세요.");

    }

    @DisplayName("로그인에 성공하면 index, 닉네임, 토큰을 반환한다.")
    @Test
    public void loginResponse() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("qwer1234")
                .build();

        // when
        ApiResponse<MemberLoginResponse> login = memberService.login(request);

        // then
        assertThat(login.getStatus())
                .isEqualTo(HttpStatus.OK);
        assertThat(login.getMessage())
                .isEqualTo("로그인에 성공하셨습니다.");
        assertThat(login.getData().getMemberNickname())
                .isEqualTo("J크");
        assertThat(jwtService.isTokenValid(
                login.getData().getMemberToken(),
                memberRepository.findByMemberId(login.getData().getMemberId())
        )).isTrue();

    }

    @DisplayName("memberId, 수정할 닉네임을 받아 닉네임을 수정한다.")
    @Test
    public void updateNickname() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("qwer1234")
                .build();

        ApiResponse<MemberLoginResponse> login = memberService.login(request);
        Long memberId = login.getData().getMemberId();

        MemberUpdateNicknameRequest memberUpdateNicknameRequest = MemberUpdateNicknameRequest.builder()
                .memberId(memberId)
                .memberNickname("섭섭")
                .build();
        ApiResponse<String> stringApiResponse = memberService.updateNickname(memberUpdateNicknameRequest);

        // when
        ApiResponse<MemberLoginResponse> login2 = memberService.login(request);

        // then
        assertThat(stringApiResponse.getCode()).isEqualTo(200);
        assertThat(login2.getData().getMemberNickname()).isEqualTo("섭섭");
    }

    @DisplayName("수정할 닉네임이 이미 존재할 경우 예외처리한다.")
    @Test
    public void updateNicknameDuplicate() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("qwer1234")
                .build();

        ApiResponse<MemberLoginResponse> login = memberService.login(request);
        Long memberId = login.getData().getMemberId();

        MemberUpdateNicknameRequest memberUpdateNicknameRequest = MemberUpdateNicknameRequest.builder()
                .memberId(memberId)
                .memberNickname("J크")
                .build();

        // when
        ApiResponse<String> stringApiResponse = memberService.updateNickname(memberUpdateNicknameRequest);

        // then
        assertThat(stringApiResponse.getCode()).isEqualTo(400);
        assertThat(stringApiResponse.getMessage()).isEqualTo("이미 사용 중인 닉네임 입니다.");
    }

    @DisplayName("memberId, 비밀번호, 수정할 비밀번호를 받아 비밀번호를 수정한다.")
    @Test
    public void updatePassword() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("qwer1234")
                .build();

        ApiResponse<MemberLoginResponse> login = memberService.login(request);
        Long memberId = login.getData().getMemberId();

        MemberUpdatePasswordRequest memberUpdatePasswordRequest = MemberUpdatePasswordRequest.builder()
                .memberId(memberId)
                .memberOriginPassword("qwer1234")
                .memberChangePassword("1234qwer")
                .build();
        ApiResponse<String> stringApiResponse = memberService.updatePassword(memberUpdatePasswordRequest);

        MemberLoginRequest request2 = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("1234qwer")
                .build();

        ApiResponse<MemberLoginResponse> login2 = memberService.login(request2);
        // when
        Member byMemberId = memberRepository.findByMemberId(login2.getData().getMemberId());

        // then
        assertThat(stringApiResponse.getCode()).isEqualTo(200);
        assertThat(passwordEncoder.matches(memberUpdatePasswordRequest.getMemberChangePassword(), byMemberId.getMemberPassword())).isTrue();
    }

    @DisplayName("비밀번호 수정 시 원래 비밀번호를 인증 하여 실패 시 예외처리한다.")
    @Test
    public void updatePasswordWrong() throws Exception {
        // given
        MemberSignupRequest member1 = MemberSignupRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberNickname("J크")
                .memberPassword("qwer1234")
                .memberBirthDate(LocalDate.now())
                .memberName("Jake")
                .build();

        memberService.signUp(member1);

        MemberLoginRequest request = MemberLoginRequest.builder()
                .memberEmail("Jake95@naver.com")
                .memberPassword("qwer1234")
                .build();

        ApiResponse<MemberLoginResponse> login = memberService.login(request);
        Long memberId = login.getData().getMemberId();

        MemberUpdatePasswordRequest memberUpdatePasswordRequest = MemberUpdatePasswordRequest.builder()
                .memberId(memberId)
                .memberOriginPassword("qwer12345")
                .memberChangePassword("1234qwer")
                .build();

        // when
        ApiResponse<String> stringApiResponse = memberService.updatePassword(memberUpdatePasswordRequest);

        // then
        assertThat(stringApiResponse.getCode()).isEqualTo(401);
        assertThat(stringApiResponse.getMessage()).isEqualTo("비밀번호가 잘못되었습니다.");

    }

}