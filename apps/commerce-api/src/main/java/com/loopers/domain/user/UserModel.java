package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter@Setter
public class UserModel extends BaseEntity {

    private String loginId;
    private String email;
    private String birth;
    private String grender;


    private static final String REGEX = "^[a-zA-Z0-9]{1,10}$";
    private static final String EREGEX = "^\\S+@\\S+\\.\\S+$";



    public UserModel(String loginId, String email, String birth, String grender) {
        if(!loginId.matches(REGEX)) {
            throw new CoreException(ErrorType.BAD_REQUEST,"loginId는 영문 및 숫자 10자 이내로 입력해야 합니다.");
        }
        if(!email.matches(EREGEX)) {
            throw new CoreException(ErrorType.BAD_REQUEST,"email은 올바른 형식으로 입력해야 합니다.");
        }
        if(!validBirth(birth)) {
            throw new CoreException(ErrorType.BAD_REQUEST,"birth는 yyyy-MM-dd 형식으로 입력해야 합니다.");
        }
        this.loginId = loginId;
        this.email = email;
        this.birth = birth;
        this.grender = grender;
    }
    private boolean validBirth(String birth) {
        try {
            LocalDate localDate = LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if(localDate.isAfter(LocalDate.now())){
                throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 현재 날짜 이전이어야 합니다.");
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
