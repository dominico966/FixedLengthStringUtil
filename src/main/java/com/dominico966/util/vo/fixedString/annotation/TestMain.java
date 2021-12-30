package com.dominico966.util.vo.fixedString.annotation;

import com.dominico966.util.vo.fixedString.annotation.handler.DataType;
import com.dominico966.util.vo.fixedString.annotation.handler.FixedLengthStringAnnotationHandler;
import lombok.Data;

public class TestMain {
    public static void main(String...args) {
        String message = "KCIMYDTOR0863716550210825C2000000000000000120210201123712000000120210201123712YY12345678901234";

    }

    @Data
    public abstract class CommonFixedLengthMessage {

        // transactionCode 길이 + length 길이 == 9 + 4 == 13
        public static final int FIXED_MESSAGE_LENGTH_BIAS = 13;

        // Transaction Code
        // 운영 : 'KCIMYDTOR'
        // 개발 : 'KCIMYDTOT'
        @FixedLengthString(order = -10000, value = "9", dataType = DataType.STRING)
        private final String transactionCode = "KCIMYDTOT";

        // 전문 length
        @FixedLengthStringLength
        @FixedLengthString(order = -9990, value = "4", dataType = DataType.INTEGER)
        private int length = 0;

        // System-ID
        @FixedLengthString(order = -9980, value = "2", dataType = DataType.STRING)
        private String systemId;

        // 참여회사 회선번호
        @FixedLengthString(order = -9970, value = "3", dataType = DataType.STRING)
        private String participatingCompanyCode;

        // 전문종별 코드
        @FixedLengthString(order = -9960, value = "4", dataType = DataType.STRING)
        private String messageTypeCode;

        // 업무구분 코드
        @FixedLengthString(order = -9950, value = "3", dataType = DataType.STRING)
        private String businessClassificationCode;

        // 송수신 플래그
        @FixedLengthString(order = -9940, value = "1", dataType = DataType.STRING)
        private String sendReceiveCode;

        // 상태코드
        @FixedLengthString(order = -9930, value = "3", dataType = DataType.STRING)
        private String statusCode;

        // 점포코드
        @FixedLengthString(order = -9920, value = "7", dataType = DataType.STRING)
        private String storeCode;

        // 참여회사 전문관리 번호
        @FixedLengthString(order = -9910, value = "7", dataType = DataType.INTEGER)
        private String participatingCompanyMessageManagementNo;

        // 참여회사 전문전송 시간
        @FixedLengthString(order = -9900, value = "14", dataType = DataType.INTEGER)
        private String participatingCompanyMessageTransferDateTime;

        // 신정원 전문관리 번호
        @FixedLengthString(order = -9890, value = "7", dataType = DataType.INTEGER)
        private String kciMessageManagementNo;

        // 신정원 전문전송 시간
        @FixedLengthString(order = -9880, value = "14", dataType = DataType.STRING)
        private String kciMessageTransferDateTime;

        // 정기적 전송 여부 Y/N
        @FixedLengthString(order = -9870, value = "1", dataType = DataType.STRING)
        private String allowedTransferRegularly;

        // 전송요구권 여부 Y/N
        @FixedLengthString(order = -9860, value = "1", dataType = DataType.STRING)
        private String allowedTransferRequest;

        // 예비정보 필드
        @FixedLengthString(order = -9850, value = "14", dataType = DataType.STRING)
        private String additionalDataField;

        /*
        1) Transaction Code' 와 '2)전문 Length'를 제외한 전문의 길이를 Set
        ex)
        1024 (전체 전문길이, 가변) – 9 (Transaction Code 필드길이, 고정) – 4 (전문 Length 필드길이, 고정) = 1011 (가변)
        * */
        public int getFixedMessageLength() {
            return Math.max((FixedLengthStringAnnotationHandler.getFixedMessageLength(this) - FIXED_MESSAGE_LENGTH_BIAS), 0);
        }
}
