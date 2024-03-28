package com.ssafy.pennypal.domain.team.controller;

import com.ssafy.pennypal.bank.service.api.BankServiceAPIImpl;
import com.ssafy.pennypal.domain.chat.service.ChatService;
import com.ssafy.pennypal.domain.team.dto.request.TeamBanishRequest;
import com.ssafy.pennypal.domain.team.dto.request.TeamCreateRequest;
import com.ssafy.pennypal.domain.team.dto.request.TeamJoinRequest;
import com.ssafy.pennypal.domain.team.dto.SimpleTeamDto;
import com.ssafy.pennypal.domain.team.dto.request.TeamModifyRequest;
import com.ssafy.pennypal.domain.team.dto.response.*;
import com.ssafy.pennypal.domain.team.service.TeamService;
import com.ssafy.pennypal.global.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/team")
public class TeamController {

    private final TeamService teamService;
    private final ChatService chatService;

    private final BankServiceAPIImpl bankServiceAPI;

    /**
     * note : 2.1 팀 생성 ( + 팀 채팅방 생성 )
     */
    @PostMapping("/create")
    public ApiResponse<TeamCreateResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {

        // 팀 생성
        TeamCreateResponse result = teamService.createTeam(request.toServiceRequest());

        // 팀 채팅방 생성
        chatService.createChatRoom(request.getTeamLeaderId());

        return ApiResponse.ok(result);

    }

    /**
     * note : 매주 월요일 오전 12시에 주간 랭킹 업데이트
     */
    @Scheduled(cron = "00 00 00 * * MON")
//    @PostMapping("/rank")
    public void autoRankWeekly() {
        teamService.calculateTeamScore();
        teamService.RankTeamScore();
    }

    /**
     * note : 2.2 팀 주간 랭킹 조회
     */
    @GetMapping("/rank/weekly")
    public ApiResponse<Page<TeamRankHistoryResponse>> weeklyTeamRanking(
            @PageableDefault(page = 0, size = 6, direction = Sort.Direction.ASC)
            Pageable pageable){

        return ApiResponse.ok(teamService.rankOfWeeks(pageable));
    }

    /**
     * note : 매 시 정각에 실시간 랭킹 업데이트
     */
    @Scheduled(cron = "0 0 * * * *")
//    @PostMapping("/rankRealtime")
    public void autoRankRealtime() {

        // 팀 점수 계산
        teamService.calculateTeamScore();

        // 팀 실시간 등수 계산
        teamService.RankRealTimeScore();
    }

    /**
     * note : 2.2.1 팀 실시간 랭킹 조회
     */
    @GetMapping("/rank/realtime")
    public ApiResponse<Page<TeamRankRealtimeResponse>> realtimeTeamRanking(
            @PageableDefault(page = 0, size = 6, direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ApiResponse.ok(teamService.rankOfRealtime(pageable));
    }

    /**
     * note : 2.3 팀 전체 조회 + 검색 (팀이름)
     */
    @GetMapping
    public ApiResponse<Page<TeamSearchResponse>> searchTeamList(
            @RequestParam(name = "keyword", required = false) String teamName,
            @PageableDefault(page = 0, size = 4, sort = "teamName" , direction = Sort.Direction.ASC)
            Pageable pageable
    ){

        return ApiResponse.ok(teamService.searchTeamList(teamName, pageable));
    }

    /**
     * note : 2.4 팀 상세 조회
     */
    @GetMapping("/{teamId}")
    public ApiResponse<TeamDetailResponse> detailTeamInfo(@PathVariable Long teamId){

        return ApiResponse.ok(teamService.detailTeamInfo(teamId));
    }

    /**
     * note : 2.5 팀 정보 수정
     */
    @PatchMapping("/{teamId}")
    public ApiResponse<TeamModifyResponse> modifyTeam(@PathVariable("teamId") Long teamId,
                                                      @RequestBody TeamModifyRequest request){

        return ApiResponse.ok(teamService.modifyTeam(teamId, request));
    }

    /**
     * note : 2.5.1 팀원 추방
     * todo : 응답값 상의 후 수정
     */
    @PostMapping("/ban")
    public ApiResponse<String> banishMember(@RequestBody TeamBanishRequest request){

        teamService.banishMember(request);

        return ApiResponse.ok("추방 완료");
    }

    /**
     * note : 2.5.2 팀 가입 ( + 팀 채팅방 초대 )
     */
    @PostMapping("/join")
    public ApiResponse<TeamJoinResponse> joinTeam(@RequestBody TeamJoinRequest request) {

        TeamJoinResponse result = teamService.joinTeam(request.toServiceRequest());

        // 팀 채팅방 초대
        chatService.inviteChatRoom(request.getTeamId(), request.getMemberId());

        return ApiResponse.ok(result);

    }

    /**
     * note : 2.5.3 팀 탈퇴
     * todo : 응답값 상의 후 수정
     */
    @PostMapping("leave")
    public ApiResponse<String> leaveTeam(@RequestBody SimpleTeamDto request){

        teamService.leaveTeam(request);

        return ApiResponse.ok("탈퇴 완료");
    }

    /**
     * note : 2.6 팀 삭제
     * todo : 응답값 상의 후 수정
     */
    @DeleteMapping
    public ApiResponse<String> deleteTeam(@RequestBody SimpleTeamDto request){

        teamService.deleteTeam(request);

        return ApiResponse.ok("삭제 완료");
    }

}
