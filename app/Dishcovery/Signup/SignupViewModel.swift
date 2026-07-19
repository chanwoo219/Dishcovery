//import Foundation
//
//
//@MainActor
//class SignupViewModel: ObservableObject{
//    
//    @Published var message = ""
//    @Published private var isLoading = false
//    @Published var userId = ""
//    @Published var userPswd = ""
//    @Published var userName = ""
//    @Published var userMail = ""
//    @Published var signupSuccess = false
//    
//    @Published var showAlert = false
//    
//    
//    func signUp(userId: String, userPswd: String, userName: String, userMail: String) async {
//            guard let url = URL(string: "\(API.baseURL)/api/signup") else {
//                message = "서버 주소 오류"
//                showAlert = true
//                return
//            }
//
//            let newUser = [
//                "userId": userId,
//                "userPswd":userPswd,
//                "userName": userName,
//                "userMail": userMail`
//            ]
//
//            guard let jsonData = try? JSONSerialization.data(withJSONObject: newUser) else {
//                message = "데이터 변환 오류"
//                showAlert = true
//                return
//            }
//
//            var request = URLRequest(url: url)
//            request.httpMethod = "POST"
//            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
//            request.httpBody = jsonData
//
//            isLoading = true
//            defer { isLoading = false }
//
//            do {
//                let (data, response) = try await URLSession.shared.data(for: request)
//                            
//                guard let httpResponse = response as? HTTPURLResponse else {
//                    message = "잘못된 응답"
//                    showAlert = true
//                    return
//                }
//                
//                // 서버에서 {"message":"회원가입 성공!"} 형태로 온다고 가정
//                let json = try? JSONSerialization.jsonObject(with: data) as? [String: String]
//                let serverMessage = json?["message"] ?? "응답 파싱 오류"
//                
//                switch httpResponse.statusCode {
//                case 201:
//                    message = serverMessage
//                    signupSuccess = true
//                    showAlert = true
//                case 400, 409:
//                    message = serverMessage
//                    signupSuccess = false
//                    showAlert = true
//                default:
//                    message = "서버 오류 (\(httpResponse.statusCode))"
//                    signupSuccess = false
//                    showAlert = true
//                }
//            } catch {
//                message = "서버 통신 오류: \(error.localizedDescription)"
//                signupSuccess = false
//                showAlert = true
//                
//            }
//        }
//}
//


import Foundation

@MainActor
final class SignupViewModel: ObservableObject {

    // MARK: - Input
    @Published var userId = ""
    @Published var userPswd = ""
    @Published var userPswdConfirm = ""
    @Published var userName = ""
    @Published var userMail = ""
    @Published var verificationCode = ""

    // MARK: - UI State
    @Published var message = ""
    @Published private(set) var isLoading = false
    @Published var signupSuccess = false
    @Published var showAlert = false

    // MARK: - Endpoints
    // 너 서버에 맞게 경로만 바꿔서 쓰면 됨
    private var sendVerificationURL: URL? {
        URL(string: "\(API.baseURL)/api/send-verification")
    }

    private var verifyAndSignupURL: URL? {
        URL(string: "\(API.baseURL)/api/verify-and-signup")
    }

    private var signupURL: URL? {
        URL(string: "\(API.baseURL)/api/signup")
    }

    private var resendCodeURL: URL? {
        URL(string: "\(API.baseURL)/api/resend-code")
    }

    // MARK: - Public APIs

    /// (선택) 이메일 인증 없이 기존처럼 바로 회원가입 (네가 쓰던 /api/signup 그대로)
    func signUp() async {
        guard validateBasicFields() else { return }
        guard let url = signupURL else {
            message = "서버 주소 오류"
            showAlert = true
            return
        }

        let body: [String: Any] = [
            "userId": userId,
            "userPswd": userPswd,
            "userName": userName,
            "userMail": userMail
        ]

        await request(
            url: url,
            body: body,
            successStatusCodes: [201],
            failureStatusCodes: [400, 409],
            successMessageFallback: "회원가입 성공!",
            failureMessageFallback: "회원가입 실패"
        ) { statusCode in
            // 기존 코드 스타일 유지
            self.signupSuccess = (statusCode == 201)
        }
    }

    /// 1) 인증 메일 전송
    func sendVerificationEmail() async -> Bool {
        guard validateBasicFields() else { return false }
        guard validateEmail() else { return false }
        guard let url = sendVerificationURL else {
            message = "서버 주소 오류"
            showAlert = true
            return false
        }

        let body: [String: Any] = [
            "userMail": userMail,
            "userId": userId,
            "userName": userName,
            "userPswd": userPswd
        ]

        var ok = false
        await request(
            url: url,
            body: body,
            successStatusCodes: [200],
            failureStatusCodes: [400, 409],
            successMessageFallback: "인증 메일이 발송되었습니다.",
            failureMessageFallback: "인증 메일 발송에 실패했습니다."
        ) { statusCode in
            ok = (statusCode == 200)
        }
        return ok
    }

    /// 2) 인증 코드 검증 + 회원가입 완료
    func verifyEmailAndSignup() async {
        guard validateBasicFields() else { return }
        guard validateEmail() else { return }
        guard !verificationCode.isEmpty else {
            message = "인증 코드를 입력해주세요."
            showAlert = true
            signupSuccess = false
            return
        }
        guard let url = verifyAndSignupURL else {
            message = "서버 주소 오류"
            showAlert = true
            return
        }

        let body: [String: Any] = [
            "userMail": userMail,
            "userId": userId,
            "userName": userName,
            "userPswd": userPswd,
            "verificationCode": verificationCode
        ]

        await request(
            url: url,
            body: body,
            successStatusCodes: [200, 201],
            failureStatusCodes: [400, 401, 409],
            successMessageFallback: "회원가입이 완료되었습니다!",
            failureMessageFallback: "인증 코드가 올바르지 않습니다."
        ) { statusCode in
            self.signupSuccess = (statusCode == 200 || statusCode == 201)
        }
    }

    /// 인증 코드 재발송 (계정은 1단계에서 이미 생성됐으므로 재생성을 시도하지 않고 코드만 재요청)
    func resendVerificationEmail() async {
        guard let url = resendCodeURL else {
            message = "서버 주소 오류"
            showAlert = true
            return
        }

        await request(
            url: url,
            body: ["userId": userId],
            successStatusCodes: [200],
            failureStatusCodes: [404],
            successMessageFallback: "인증 코드를 다시 보냈습니다.",
            failureMessageFallback: "존재하지 않는 계정입니다."
        ) { _ in }
    }

    // MARK: - Private Helpers

    private func validateBasicFields() -> Bool {
        guard !userMail.isEmpty, !userId.isEmpty, !userName.isEmpty, !userPswd.isEmpty else {
            message = "모든 필드를 입력해주세요."
            showAlert = true
            signupSuccess = false
            return false
        }
        guard userPswd == userPswdConfirm else {
            message = "비밀번호가 일치하지 않습니다."
            showAlert = true
            signupSuccess = false
            return false
        }
        return true
    }

    private func validateEmail() -> Bool {
        guard userMail.contains("@") else {
            message = "올바른 이메일 형식을 입력해주세요."
            showAlert = true
            signupSuccess = false
            return false
        }
        return true
    }

    /// 공통 네트워크 요청 (서버가 {"message":"..."} 내려주는 형태 대응)
    private func request(
        url: URL,
        body: [String: Any],
        successStatusCodes: Set<Int>,
        failureStatusCodes: Set<Int>,
        successMessageFallback: String,
        failureMessageFallback: String,
        onComplete: @MainActor (_ statusCode: Int) -> Void
    ) async {
        guard let jsonData = try? JSONSerialization.data(withJSONObject: body) else {
            message = "데이터 변환 오류"
            showAlert = true
            signupSuccess = false
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData

        isLoading = true
        defer { isLoading = false }

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse else {
                message = "잘못된 응답"
                showAlert = true
                signupSuccess = false
                return
            }

            let json = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any]
            let serverMessage = (json?["message"] as? String)

            let code = httpResponse.statusCode

            if successStatusCodes.contains(code) {
                message = serverMessage ?? successMessageFallback
                showAlert = true
                await onComplete(code)
            } else if failureStatusCodes.contains(code) {
                message = serverMessage ?? failureMessageFallback
                showAlert = true
                await onComplete(code)
            } else {
                message = "서버 오류 (\(code))"
                showAlert = true
                signupSuccess = false
                await onComplete(code)
            }
        } catch {
            message = "서버 통신 오류: \(error.localizedDescription)"
            showAlert = true
            signupSuccess = false
        }
    }
}
