import { closeTeamSettingModal, deleteTeam, getTeamWaitingList } from '@/pages/teamInfo/index';
import { useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';

type TeamSettingModal = {
    teamId: number;
    memberId: number;
    teamName?: string;
    teamInfo?: string;
    members?: [];
};

export function TeamSettingModal({ teamId, memberId, teamName, teamInfo, members }: TeamSettingModal) {
    const [waitingList, setWaitingList] = useState([]);
    const dispatch = useDispatch();

    useEffect(() => {
        const postDto = {
            teamId,
            memberId,
        };

        // if: 팀 가입이 수동 승인일 때
        // if (!teamIsAutoConfirm) {
        // 1. 가입 승인 대기자 리스트 가져오기
        getTeamWaitingList(postDto).then((res) => {
            if (res.data.code === 200) {
                setWaitingList(res.data.data);
            }
        });
        // }
    }, []);

    return (
        <div className="modalContainer">
            <div className="teamSettingModal">
                <div className="teamSettingModal__middle">
                    <div className="teamSettingModal__middle-info">
                        <div className="teamSettingModal__middle-info-teamName">
                            <div className="teamSettingModal__middle-info-teamName-key">팀명</div>
                            <div className="teamSettingModal__middle-info-teamName-value">{teamName}</div>
                        </div>
                        <div className="teamSettingModal__middle-info-teamInfo">
                            <div className="teamSettingModal__middle-info-teamInfo-key">팀소개</div>
                            <input
                                className="teamSettingModal__middle-info-teamInfo-value"
                                defaultValue={teamInfo ?? '팀 소개말이 없습니다.'}
                            />
                        </div>
                        <div className="teamSettingModal__middle-info-teamIsAutoConfirm">
                            <div className="teamSettingModal__middle-info-teamIsAutoConfirm-key">
                                가입 자동 승인 여부
                            </div>
                            <div className="teamSettingModal__middle-info-teamIsAutoConfirm-value">
                                <div>YES</div>
                                <div>NO</div>
                            </div>
                        </div>
                        <div className="teamSettingModal__middle-info-modify">
                            <button className="teamSettingModal__middle-info-modify-button">수정하기</button>
                        </div>
                    </div>
                    <hr />
                    <div className="teamSettingModal__middle-personnel">
                        <div className="teamSettingModal__middle-personnel-current">
                            <div>팀원 현황</div>
                            <div className="teamSettingModal__middle-personnel-current-list">
                                <MemberListItem />
                                <MemberListItem />
                                <MemberListItem />
                                <MemberListItem />
                                <MemberListItem />
                                <MemberListItem />
                            </div>
                        </div>
                        <div className="teamSettingModal__middle-personnel-waiting">
                            <div>가입 대기자</div>
                            <div className="teamSettingModal__middle-personnel-waiting-list">
                                <WaitingMemberListItem />
                                <WaitingMemberListItem />
                                <WaitingMemberListItem />
                                <WaitingMemberListItem />
                            </div>
                        </div>
                    </div>
                </div>
                <div className="teamSettingModal__bottom">
                    <div className="teamSettingModal__bottom-buttons">
                        <button
                            onClick={async () => {
                                const deleteDto = { teamId, memberId };
                                const res = await deleteTeam(deleteDto);
                            }}
                        >
                            팀삭제하기
                        </button>
                        <button
                            onClick={() => {
                                dispatch(closeTeamSettingModal());
                            }}
                        >
                            나가기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

function MemberListItem() {
    return (
        <div className="memberListItem">
            <div className="memberListItem-name">멤버이름</div>
            <button className="memberListItem-ban">추방</button>
        </div>
    );
}

function WaitingMemberListItem() {
    return (
        <div className="waitingMemberListItem">
            <div className="waitingMemberListItem-name">멤버이름</div>
            <div className="waitingMemberListItem-buttons">
                <button className="waitingMemberListItem-buttons-button">승인</button>
                <button className="waitingMemberListItem-buttons-button">거절</button>
            </div>
        </div>
    );
}
