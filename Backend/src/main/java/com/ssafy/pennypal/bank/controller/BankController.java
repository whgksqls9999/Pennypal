package com.ssafy.pennypal.bank.controller;

import com.ssafy.pennypal.bank.dto.controller.UserDummyAccount;
import com.ssafy.pennypal.bank.dto.controller.request.AccountDrawingTransferControllerDTO;
import com.ssafy.pennypal.bank.dto.controller.request.AccountTransactionRequestDTO;
import com.ssafy.pennypal.bank.dto.controller.request.AccountTransferRequestDTO;
import com.ssafy.pennypal.bank.dto.controller.response.AccountTransactionResponseDTO;
import com.ssafy.pennypal.bank.dto.controller.response.UserAccountResponseControllerDTO;
import com.ssafy.pennypal.bank.dto.controller.response.UserAccountsResponseControllerDTO;
import com.ssafy.pennypal.bank.dto.controller.response.UserCreateAccountControllerDTO;
import com.ssafy.pennypal.bank.dto.dummy.DummyTransactionSummary;
import com.ssafy.pennypal.bank.dto.service.common.CommonHeaderRequestDTO;
import com.ssafy.pennypal.bank.dto.service.request.*;
import com.ssafy.pennypal.bank.service.api.IBankServiceAPI;
import com.ssafy.pennypal.bank.service.db.IBankServiceDB;
import com.ssafy.pennypal.global.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankController {
    private static final String SSAFY_BANK_API_KEY = System.getenv("SSAFY_BANK_API_KEY");

    private final IBankServiceAPI bankServiceAPI;
    private final IBankServiceDB bankServiceDB;

    // 유효성 검사 오류 메시지를 처리해서 ApiResponse 객체를 반환하는 메서드
    private ApiResponse<Object> getValidationErrorResponse(BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ObjectError error : result.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    errorMessage.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
                } else {
                    errorMessage.append(error.getDefaultMessage()).append("; ");
                }
            }
            log.info("Validation errors: {}", errorMessage);
            return ApiResponse.of(HttpStatus.BAD_REQUEST, errorMessage.toString(), null);
        }
        return null;
    }

    @PostMapping("/user/key/{userEmail}")
    public ApiResponse<Object> createUserApiKey(@PathVariable String userEmail) {
        UserAccountRequestServiceDTO userAccountRequestServiceDTO = UserAccountRequestServiceDTO.builder()
                .apiKey(SSAFY_BANK_API_KEY)
                .userId(userEmail)
                .build();

        UserAccountResponseControllerDTO userAccountResponseControllerDTO = UserAccountResponseControllerDTO.of(
                bankServiceAPI.createUserAccount(userAccountRequestServiceDTO)
        );

        UserApiKeyRequestDTO userApiKeyRequestDTO = UserApiKeyRequestDTO.builder()
                .userId(userAccountResponseControllerDTO.getUserEmail())
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        log.info("여기까지 됬어요");
        bankServiceDB.InsertUserKey(userApiKeyRequestDTO);

        CommonHeaderRequestDTO commonHeaderRequestDTO = CommonHeaderRequestDTO.builder()
                .apiName("openAccount")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        UserBankAccountRequestServiceDTO userBankAccountRequestServiceDTO = UserBankAccountRequestServiceDTO.builder()
                .header(commonHeaderRequestDTO)
                .accountTypeUniqueNo("001-1-81fe2deafd1943")
                .build();

        UserCreateAccountControllerDTO userBankAccount = UserCreateAccountControllerDTO.of(
                bankServiceAPI.createUserBankAccount(userBankAccountRequestServiceDTO)
        );

        CommonHeaderRequestDTO commonHeaderRequestDTO2 = CommonHeaderRequestDTO.builder()
                .apiName("receivedTransferAccountNumber")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        AccountDepositServiceDTO accountDepositServiceDTO = AccountDepositServiceDTO.builder()
                .header(commonHeaderRequestDTO2)
                .bankCode(userBankAccount.getREC().getBankCode())
                .accountNo(userBankAccount.getREC().getAccountNo())
                .transactionBalance("100000000")
                .transactionSummary("초기 입금")
                .build();

        bankServiceAPI.accountDeposit(accountDepositServiceDTO);

        IntStream.range(0, 10).forEach(i -> {
            CommonHeaderRequestDTO commonHeaderRequestDTO3 = CommonHeaderRequestDTO.builder()
                    .apiName("drawingTransfer")
                    .apiKey(SSAFY_BANK_API_KEY)
                    .userKey(userAccountResponseControllerDTO.getUserKey())
                    .build();

            DrawingTransferRequestServiceDTO drawingTransferRequestServiceDTO = DrawingTransferRequestServiceDTO.builder()
                    .header(commonHeaderRequestDTO3)
                    .bankCode(userBankAccount.getREC().getBankCode())
                    .accountNo(userBankAccount.getREC().getAccountNo())
                    .transactionBalance(generateRandomAmount())
                    .transactionSummary(DummyTransactionSummary.getRandomData()) // Enum 사용 시 getValue() 메서드 호출 필요
                    .build();
            bankServiceAPI.accountWithdrawal(drawingTransferRequestServiceDTO);
        });

        return ApiResponse.ok(userAccountResponseControllerDTO);
    }

    @GetMapping("/user/key/{userEmail}")
    public ApiResponse<Object> getUserApiKey(@PathVariable String userEmail) {
        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(userEmail);

        return ApiResponse.ok(userAccountResponseControllerDTO);
    }

    @GetMapping("/user/account/{userEmail}")
    public ApiResponse<Object> getAccountList(@PathVariable String userEmail) {

        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(userEmail);

        CommonHeaderRequestDTO commonHeaderRequestDTO = CommonHeaderRequestDTO.builder()
                .apiName("inquireAccountList")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        GetUserAccountListServiceRequestDTO getUserAccountListServiceRequestDTO = GetUserAccountListServiceRequestDTO.builder()
                .header(commonHeaderRequestDTO)
                .build();

        UserAccountsResponseControllerDTO userAccountsResponseControllerDTO = UserAccountsResponseControllerDTO.of(
                bankServiceAPI.getUserAccountList(getUserAccountListServiceRequestDTO)
        );

        return ApiResponse.ok(userAccountsResponseControllerDTO);
    }

    @PostMapping("/user/account/transaction")
    public ApiResponse<Object> getAccountTransaction(@RequestBody @Validated AccountTransactionRequestDTO requestDTO, BindingResult result) {
        ApiResponse<Object> validationResponse = getValidationErrorResponse(result);
        if (validationResponse != null)
            return validationResponse;

        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(requestDTO.getUserEmail());

        CommonHeaderRequestDTO commonHeaderRequestDTO = CommonHeaderRequestDTO.builder()
                .apiName("inquireAccountTransactionHistory")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        AccountTransactionRequestServiceDTO accountTransactionRequestServiceDTO = AccountTransactionRequestServiceDTO.builder()
                .header(commonHeaderRequestDTO)
                .bankCode(requestDTO.getBankCode())
                .accountNo(requestDTO.getAccountNo())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .transactionType(requestDTO.getTransactionType())
                .orderByType("DESC")
                .build();

        AccountTransactionResponseDTO accountTransactionResponseDTO = AccountTransactionResponseDTO.of(
                bankServiceAPI.getUserAccountTransaction(accountTransactionRequestServiceDTO)
        );

        return ApiResponse.ok(accountTransactionResponseDTO);
    }

    @PostMapping("/user/stock/account/{userEmail}")
    public ApiResponse<Object> createStockAccount(@PathVariable String userEmail) {

        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(userEmail);

        CommonHeaderRequestDTO commonHeaderRequestDTO = CommonHeaderRequestDTO.builder()
                .apiName("openAccount")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        UserBankAccountRequestServiceDTO userBankAccountRequestServiceDTO = UserBankAccountRequestServiceDTO.builder()
                .header(commonHeaderRequestDTO)
                .accountTypeUniqueNo("004-1-74fe2deafd3277")
                .build();

        UserCreateAccountControllerDTO userCreateAccountControllerDTO = UserCreateAccountControllerDTO.of(
                bankServiceAPI.createUserBankAccount(userBankAccountRequestServiceDTO)
        );

        return ApiResponse.ok(userCreateAccountControllerDTO);
    }

    @PostMapping("/user/accountTransfer")
    public ApiResponse<Object> accountTransfer(@RequestBody @Validated AccountTransferRequestDTO accountTransferRequestDTO, BindingResult result) {
        ApiResponse<Object> validationResponse = getValidationErrorResponse(result);
        if (validationResponse != null)
            return validationResponse;

        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(accountTransferRequestDTO.getUserEmail());

        CommonHeaderRequestDTO accountTransfer = CommonHeaderRequestDTO.builder()
                .apiName("accountTransfer")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        AccountTransferServiceRequestDTO accountTransferServiceRequestDTO = AccountTransferServiceRequestDTO.builder()
                .header(accountTransfer)
                .depositBankCode(accountTransferRequestDTO.getDepositBankCode())
                .depositAccountNo(accountTransferRequestDTO.getDepositAccountNo())
                .depositTransactionSummary(accountTransferRequestDTO.getDepositTransactionSummary())
                .transactionBalance(accountTransferRequestDTO.getTransactionBalance())
                .withdrawalBankCode(accountTransferRequestDTO.getWithdrawalBankCode())
                .withdrawalAccountNo(accountTransferRequestDTO.getWithdrawalAccountNo())
                .withdrawalTransactionSummary(accountTransferRequestDTO.getWithdrawalTransactionSummary())
                .build();

        bankServiceAPI.accountTransfer(accountTransferServiceRequestDTO);

        return ApiResponse.ok(null);
    }

    @PostMapping("/user/drawingTransfer")
    private ApiResponse<Object> AccountDrawingTransfer(@RequestBody @Validated AccountDrawingTransferControllerDTO accountDrawingTransferControllerDTO) {
        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(accountDrawingTransferControllerDTO.getUserEmail());

        CommonHeaderRequestDTO commonHeaderRequestDTO = CommonHeaderRequestDTO.builder()
                .apiName("drawingTransfer")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        DrawingTransferRequestServiceDTO drawingTransferRequestServiceDTO = DrawingTransferRequestServiceDTO.builder()
                .header(commonHeaderRequestDTO)
                .bankCode(accountDrawingTransferControllerDTO.getBankCode())
                .accountNo(accountDrawingTransferControllerDTO.getAccountNo())
                .transactionBalance(accountDrawingTransferControllerDTO.getTransactionBalance())
                .transactionSummary(accountDrawingTransferControllerDTO.getTransactionSummary())
                .build();

        bankServiceAPI.accountWithdrawal(drawingTransferRequestServiceDTO);

        return ApiResponse.ok(null);
    }

    @PostMapping("/user/dummy/{userEmail}")
    private ApiResponse<Object> Dummy(@PathVariable String userEmail) {
        UserAccountResponseControllerDTO userAccountResponseControllerDTO = getUserBankApi(userEmail);
        log.info("userAccountResponseControllerDTO = {}", userAccountResponseControllerDTO);
        CommonHeaderRequestDTO commonHeaderRequestDTO = CommonHeaderRequestDTO.builder()
                .apiName("inquireAccountList")
                .apiKey(SSAFY_BANK_API_KEY)
                .userKey(userAccountResponseControllerDTO.getUserKey())
                .build();

        GetUserAccountListServiceRequestDTO getUserAccountListServiceRequestDTO = GetUserAccountListServiceRequestDTO.builder()
                .header(commonHeaderRequestDTO)
                .build();

        UserAccountsResponseControllerDTO userAccountsResponseControllerDTO = UserAccountsResponseControllerDTO.of(
                bankServiceAPI.getUserAccountList(getUserAccountListServiceRequestDTO)
        );

        UserDummyAccount userDummyAccount = UserDummyAccount.of(userAccountsResponseControllerDTO);

        log.info("userDummyAccount= {}", userDummyAccount);
        IntStream.range(0, 10).forEach(i -> {
            CommonHeaderRequestDTO commonHeaderRequestDTO3 = CommonHeaderRequestDTO.builder()
                    .apiName("drawingTransfer")
                    .apiKey(SSAFY_BANK_API_KEY)
                    .userKey(userAccountResponseControllerDTO.getUserKey())
                    .build();

            DrawingTransferRequestServiceDTO drawingTransferRequestServiceDTO = DrawingTransferRequestServiceDTO.builder()
                    .header(commonHeaderRequestDTO3)
                    .bankCode(userDummyAccount.getBankCode())
                    .accountNo(userDummyAccount.getAccountNo())
                    .transactionBalance(generateRandomAmount())
                    .transactionSummary(DummyTransactionSummary.getRandomData()) // Enum 사용 시 getValue() 메서드 호출 필요
                    .build();
            
            bankServiceAPI.accountWithdrawal(drawingTransferRequestServiceDTO);
        });

        return ApiResponse.ok(null);
    }

    //common method
    private UserAccountResponseControllerDTO getUserBankApi(String userEmail) {
        UserAccountRequestServiceDTO userAccountRequestServiceDTO = UserAccountRequestServiceDTO.builder()
                .apiKey(SSAFY_BANK_API_KEY)
                .userId(userEmail)
                .build();

        return UserAccountResponseControllerDTO.of(
                bankServiceAPI.getUserAccount(userAccountRequestServiceDTO)
        );
    }

    public String generateRandomAmount() {
        Random random = new Random();
        int min = 1000;
        int max = 50000;
        return String.valueOf(random.nextInt((max - min) / 1000 + 1) * 1000 + min);
    }
}
