import React from 'react';
import { TeamApplyModal } from '@/pages/team/index';
import { RootState } from '@/app/appProvider';
import { useSelector } from 'react-redux';
import { MarketItemModal } from '@/pages/market/index';
import { TeamChattingModal, TeamLeaveModal } from '@/pages/teamInfo';
import { TeamSettingModal } from '@/pages/teamInfo';

export function ModalSpace() {
    return (
        <>
            <MarketItemModalSpace />
            <TeamApplyModalSpace />
            <TeamLeaveModalSpace />
            <TeamSettingModalSpace />
            <TeamChattingModalSpace />
        </>
    );
}

function TeamApplyModalSpace() {
    const openTeamDetailModal = useSelector((state: RootState) => state.openTeamDetailModalReducer.data);

    if (openTeamDetailModal) return <TeamApplyModal team={openTeamDetailModal} />;
    return null;
}

function MarketItemModalSpace() {
    const openMarketItemModal = useSelector((state: RootState) => state.openMarketItem.data);

    if (openMarketItemModal) {
        const data = openMarketItemModal;
        if (data instanceof Object) {
            return (
                <MarketItemModal
                    productBrand={data.productBrand}
                    productCategory={data.productBrand}
                    productId={data.productId}
                    productImg={data.productImg}
                    productPrice={data.productPrice}
                    productQuantity={data.productQuantity}
                    productName={data.productName}
                />
            );
        }
    }
    return null;
}

function TeamLeaveModalSpace() {
    const isOpenTeamLeaveModal = useSelector((state: RootState) => state.openTeamLeaveModalReducer.data);

    if (isOpenTeamLeaveModal instanceof Object)
        return <TeamLeaveModal teamId={isOpenTeamLeaveModal.teamId} memberId={isOpenTeamLeaveModal.memberId} />;
    return null;
}

function TeamSettingModalSpace() {
    const isOpenTeamSettingModal = useSelector((state: RootState) => state.openTeamSettingModalReducer.data);

    if (isOpenTeamSettingModal instanceof Object) {
        const data = isOpenTeamSettingModal;
        return (
            <TeamSettingModal
                teamId={data.teamId}
                memberId={data.memberId}
                teamName={data.teamName}
                teamInfo={data.teamInfo}
                members={data.members}
                teamIsAutoConfirm={data.teamIsAutoConfirm}
            />
        );
    }

    return null;
}

function TeamChattingModalSpace() {
    const isOpenTeamChattingModal = useSelector((state: RootState) => state.openTeamChattingModalReducer.data);

    if (isOpenTeamChattingModal instanceof Object) {
        const data = isOpenTeamChattingModal;
        return (
            <TeamChattingModal
                teamId={data.teamId}
                memberId={data.memberId}
                chatRoomId={data.chatRoomId}
                client={data.client}
            />
        );
    }
    return null;
}
