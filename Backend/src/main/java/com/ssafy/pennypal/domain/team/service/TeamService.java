package com.ssafy.pennypal.domain.team.service;

import com.ssafy.pennypal.domain.member.entity.Member;
import com.ssafy.pennypal.domain.member.repository.IMemberRepository;
import com.ssafy.pennypal.domain.team.dto.request.TeamCreateServiceRequest;
import com.ssafy.pennypal.domain.team.dto.request.TeamJoinServiceRequest;
import com.ssafy.pennypal.domain.team.dto.response.TeamCreateResponse;
import com.ssafy.pennypal.domain.team.dto.response.TeamJoinResponse;
import com.ssafy.pennypal.domain.team.dto.response.TeamMemberDetailResponse;
import com.ssafy.pennypal.domain.team.entity.Team;
import com.ssafy.pennypal.domain.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final ITeamRepository teamRepository;
    private final IMemberRepository memberRepository;

    @Transactional
    public TeamCreateResponse createTeam(TeamCreateServiceRequest request) {
        // 유저 정보 가져오기
        Member member = memberRepository.findByMemberId(request.getTeamLeaderId());

        // 이미 존재하는 팀명이라면 예외 발생
        if (teamRepository.findByTeamName(request.getTeamName()) != null) {
            throw new IllegalArgumentException("이미 사용 중인 팀명입니다.");
        }

        // 유저가 포함된 팀 있는지 확인
        if (member.getTeam() != null) {
            throw new IllegalArgumentException("한 개의 팀에만 가입 가능합니다.");
        }

        // 팀 생성
        Team team = Team.builder()
                .teamName(request.getTeamName())
                .teamIsAutoConfirm(request.getTeamIsAutoConfirm())
                .teamLeaderId(request.getTeamLeaderId())
                .build();

        // 팀 저장
        Team savedTeam = teamRepository.save(team);

        // 유저 team 정보 수정
        member.setTeam(team);
        memberRepository.save(member);

        List<TeamMemberDetailResponse> memberDetails = team.getMembers().stream()
                .filter(Objects::nonNull) // null이 아닌 멤버만 처리
                .map(m -> new TeamMemberDetailResponse(
                        m.getMemberNickname()))
                .collect(Collectors.toList());

        return TeamCreateResponse.of(savedTeam, memberDetails);
    }

    @Transactional
    public TeamJoinResponse joinTeam(TeamJoinServiceRequest request) {

        // 팀 정보 가져오기
        Team team = teamRepository.findByTeamId(request.getTeamId());

        if (team != null) {

            // 유저 정보 조회
            Member member = memberRepository.findByMemberId(request.getMemberId());

            // 팀 인원 6명이면 예외 발생
            if (team.getMembers().size() == 6) {
                throw new IllegalArgumentException("팀 인원이 가득 찼습니다.");
            }

            // 팀 구성원에 포함 돼 있는지 확인
            if (team.getMembers().contains(member)) {
                throw new IllegalArgumentException("이미 가입한 팀입니다.");
            } else {
                // 이미 다른 팀의 구성원인지 확인
                if (member.getTeam() != null) {
                    throw new IllegalArgumentException("이미 가입된 팀이 있습니다.");
                }
            }

            // 팀 자동승인 여부에 따라...
            if (team.getTeamIsAutoConfirm()) {
                // 자동 승인이라면 바로 추가
                team.getMembers().add(member);
                member.setTeam(team);
                teamRepository.save(team);
                memberRepository.save(member);
            } else {
                // 수동 승인이라면 대기 리스트에 추가하고 예외 던져주기
                team.getTeamWaitingList().add(member);
                member.setMemberWaitingTeam(team);
                throw new IllegalArgumentException("가입 요청이 완료되었습니다.");
            }

        } else {
            throw new IllegalArgumentException("팀 정보를 찾을 수 없습니다.");
        }

        List<TeamMemberDetailResponse> memberDetails = team.getMembers().stream()
                .filter(Objects::nonNull) // null이 아닌 멤버만 처리
                .map(member -> new TeamMemberDetailResponse(
                        member.getMemberNickname()))
                .collect(Collectors.toList());

        return TeamJoinResponse.builder()
                .teamName(team.getTeamName())
                .teamInfo(team.getTeamInfo())
                .teamScore(team.getTeamScore())
                .teamLeaderId(team.getTeamLeaderId())
                .members(memberDetails)
                .build();
    }

    @Transactional
    public Integer calculateTeamScore(Long teamId) {

        Team team = teamRepository.findByTeamId(teamId);

        // 팀원들의 지난 주와 이번 주 지출 내역을 모두 더하기
        Double lastWeekTotalExpenses = team.getMembers().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Member::getMemberLastWeekExpenses)
                .sum();
        Double thisWeekTotalExpenses = team.getMembers().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Member::getMemberThisWeekExpenses)
                .sum();

        // 팀원들의 출석 횟수 모두 더하기
        Double totalAttendance = team.getMembers().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Member::getMemberAttendance)
                .sum();

        // 절약 점수 계산
        Integer savingScore = calculateSavingScore(lastWeekTotalExpenses, thisWeekTotalExpenses);

        // 출석 점수 계산
        Integer attendanceScore = calculateAttendanceScore(totalAttendance, team.getMembers().size());


        // 팀 점수 저장
        team.setTeamScore(savingScore + attendanceScore);

        return savingScore + attendanceScore;

    }

    /**
     * 정렬된 팀 리스트를 반환
     */
    public List<Team> rankTeams(){

        List<Team> teamList = teamRepository.findAll();

        return teamList.stream()
                .sorted(Comparator.comparing(Team::getTeamScore).reversed())
                .collect(Collectors.toList());
    }


    private Integer calculateSavingScore(Double lastWeekTotalExpenses, Double thisWeekTotalExpenses) {

        // 이번주 지출이 지난주 지출과 같거나 크다면 절약점수는 0
        if (thisWeekTotalExpenses >= lastWeekTotalExpenses) {
            return 0;
        } else {
            double savingsScore = ((double) (lastWeekTotalExpenses - thisWeekTotalExpenses) / lastWeekTotalExpenses) * 100;
            // 정수로 변환하여 반환
            return (int) savingsScore;
        }
    }

    private Integer calculateAttendanceScore(Double totalAttendance, Integer memberCount) {
        double savingScore = ((double)totalAttendance / (memberCount * 7)) * 100;
        return (int) savingScore;
    }

}