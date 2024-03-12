package com.ssafy.pennypal.domain.market.entity;

import com.ssafy.pennypal.domain.member.entity.Member;
import com.ssafy.pennypal.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_info_id")
    private Long orderInfoId;                                   // 주문정보 Id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member memberId;                                    // 주문자

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REMOVE}
    )
    @JoinColumn(name = "product_id")
    private Product productId;                                // 주문 상품

    @Column(name = "buy_quantity")
    private Integer buyQuantity;                                // 상품 주문 갯수

    //금액이 수정 될 수 있잖어
    private Integer price;                                  // 상품 가격
}