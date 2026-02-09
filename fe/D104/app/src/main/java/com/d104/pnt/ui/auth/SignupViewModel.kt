package com.d104.pnt.ui.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.response.SignupResponse
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // 입력 상태
    private val _id = MutableStateFlow("")
    val id: StateFlow<String> = _id.asStateFlow()

    private val _pw = MutableStateFlow("")
    val pw: StateFlow<String> = _pw.asStateFlow()

    private val _pwConfirm = MutableStateFlow("")
    val pwConfirm: StateFlow<String> = _pwConfirm.asStateFlow()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _birth = MutableStateFlow("")
    val birth: StateFlow<String> = _birth.asStateFlow()

    // 중복 체크 상태
    private val _isDuplicateChecked = MutableStateFlow(false)
    val isDuplicateChecked: StateFlow<Boolean> = _isDuplicateChecked.asStateFlow()

    private val _isDuplicated = MutableStateFlow(false)
    val isDuplicated: StateFlow<Boolean> = _isDuplicated.asStateFlow()

    // 중복 체크 로딩 상태
    private val _isDuplicateCheckLoading = MutableStateFlow(false)
    val isDuplicateCheckLoading: StateFlow<Boolean> = _isDuplicateCheckLoading.asStateFlow()

    // 유효성 검사 메시지

    // ID 유효성
    val isIdValid: StateFlow<Boolean> = _id.map {
        it.length in 5..12
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val idErrorMessage: StateFlow<String> =
        combine(_id, _isDuplicateChecked, _isDuplicated) { id, checked, duplicated ->
            when {
                id.isEmpty() -> ""
                id.length !in 5..12 -> "아이디는 5~12자 이내여야 합니다"
                !checked -> "아이디 중복 확인을 해주세요"
                duplicated -> "이미 사용 중인 아이디입니다"
                else -> ""
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // 비밀번호 유효성
    val isPwValid: StateFlow<Boolean> = _pw.map {
        it.length in 8..16
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pwErrorMessage: StateFlow<String> = _pw.map { pw ->
        when {
            pw.isEmpty() -> ""
            pw.length < 8 -> "비밀번호는 최소 8자 이상이어야 합니다"
            pw.length > 16 -> "비밀번호는 최대 16자까지 가능합니다"
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // 비밀번호 확인 유효성
    val isPwConfirmValid: StateFlow<Boolean> = combine(_pw, _pwConfirm) { pw, confirm ->
        pw.isNotEmpty() && pw == confirm
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pwConfirmErrorMessage: StateFlow<String> = combine(_pw, _pwConfirm) { pw, confirm ->
        when {
            confirm.isEmpty() -> ""
            pw != confirm -> "비밀번호가 일치하지 않습니다"
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // 닉네임 유효성
    val isNicknameValid: StateFlow<Boolean> = _nickname.map {
        it.length in 2..10
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val nicknameErrorMessage: StateFlow<String> = _nickname.map { nickname ->
        when {
            nickname.isEmpty() -> ""
            nickname.length < 2 -> "닉네임은 최소 2자 이상이어야 합니다"
            nickname.length > 10 -> "닉네임은 최대 10자까지 가능합니다"
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // 생년월일 유효성
    val isBirthValid: StateFlow<Boolean> = birth.map { birthValue ->
        val isValid = birthValue.length == 10
        isValid
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val birthErrorMessage: StateFlow<String> =
        birth.map { birthValue ->
            when {
                birthValue.isEmpty() -> ""
                birthValue.length < 10 -> ""
                else -> ""
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ""
        )

    // 회원가입 상태
    private val _signupState = MutableStateFlow<UiState<SignupResponse>>(UiState.Idle)
    val signupState: StateFlow<UiState<SignupResponse>> = _signupState.asStateFlow()

    // 입력값 업데이트
    fun updateId(newId: String) {
        _id.value = newId
        _isDuplicateChecked.value = false
        _isDuplicated.value = false
    }

    fun updatePw(newPw: String) {
        _pw.value = newPw
    }

    fun updatePwConfirm(newPwConfirm: String) {
        _pwConfirm.value = newPwConfirm
    }

    fun updateNickname(newNickname: String) {
        _nickname.value = newNickname
    }

    fun updateBirth(newBirth: String) {
        _birth.value = newBirth
    }

    // 중복 체크
    fun checkDuplicate() {
        viewModelScope.launch {

            if (!isIdValid.value) {
                return@launch
            }

            _isDuplicateCheckLoading.value = true

            try {
                when (val result = authRepository.checkDuplicate(_id.value)) {
                    is BaseResult.Success -> {
                        _isDuplicateChecked.value = true
                        _isDuplicated.value = result.data.duplicated
                    }

                    is BaseResult.Error -> {
                        _isDuplicateChecked.value = false
                    }
                }
            } catch (e: Exception) {
                _isDuplicateChecked.value = false
            } finally {
                _isDuplicateCheckLoading.value = false
            }
        }
    }

    // 유효성 검사
    fun isSignupEnabled(): Boolean {
        val idCheck = _id.value.length in 5..12
        val pwCheck = _pw.value.length in 8..16
        val pwMatchCheck = _pw.value == _pwConfirm.value
        val nicknameCheck = _nickname.value.length in 2..10
        val birthCheck = _birth.value.length == 10
        val duplicateCheck = _isDuplicateChecked.value
        val notDuplicatedCheck = !_isDuplicated.value

        val result = idCheck && pwCheck && pwMatchCheck && nicknameCheck &&
                birthCheck && duplicateCheck && notDuplicatedCheck

        return result
    }

    // 회원가입
    fun signup() {
        viewModelScope.launch {

            if (!isSignupEnabled()) {
                return@launch
            }

            _signupState.value = UiState.Loading

            val formattedBirth = formatBirthForServer(_birth.value)

            try {
                when (val result = authRepository.signup(
                    id = _id.value,
                    password = _pw.value,
                    passwordConfirm = _pwConfirm.value,
                    nickname = _nickname.value,
                    email = "",
                    birth = formattedBirth,
                    avatarUrl = null
                )) {
                    is BaseResult.Success -> {
                        _signupState.value = UiState.Success(result.data)
                    }

                    is BaseResult.Error -> {
                        _signupState.value = UiState.Error(result.error.message)
                    }
                }
            } catch (e: Exception) {
                _signupState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetSignupState() {
        _signupState.value = UiState.Idle
    }
}

private fun formatBirthForServer(birth: String): String {
    return birth.replace(".", "-")
}