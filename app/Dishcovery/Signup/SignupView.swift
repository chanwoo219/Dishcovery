//import SwiftUI
//
//
//struct SignupView: View {
//
//    @Binding var path: NavigationPath
//    @StateObject var viewModel = SignupViewModel()
//    @State private var isLoading = false
//
//    @Environment(\.dismiss) var dismiss
//
//    var body: some View {
//        VStack(spacing: 20) {
//            Text("회원가입")
//                .font(.largeTitle)
//                .fontWeight(.bold)
//                .padding(.top, 50)
//            
//            Group {
//                
//                TextField("이메일", text: $viewModel.userMail)
////                    .textFieldStyle(RoundedBorderTextFieldStyle())
//                    .padding()
//                    .autocapitalization(.none)
//                    .keyboardType(.emailAddress)
//                    .background(Color(.systemGray6))
//                    .cornerRadius(10)
////                TextField("아이디", text: $viewModel.userId)
////                    .textFieldStyle(RoundedBorderTextFieldStyle())
////                    .autocapitalization(.none)
//                
//                TextField("아이디", text: $viewModel.userId)
//                    .padding()
//                    .background(Color(.systemGray6))
//                    .cornerRadius(10)
//                    .textInputAutocapitalization(.never)
//                    .autocorrectionDisabled(true)
//                
//                TextField("이름", text: $viewModel.userName)
//                    .padding()
////                    .textFieldStyle(RoundedBorderTextFieldStyle())
//                    .background(Color(.systemGray6))
//                    .cornerRadius(10)
//                    .textInputAutocapitalization(.never)
////                    .disableAutocorrection(true)
//                
//                SecureField("비밀번호", text: $viewModel.userPswd)
//                    .padding()
//                    .background(Color(.systemGray6))
//                    .cornerRadius(10)
//            }
//            .padding(.horizontal)
//            
//            if !viewModel.message.isEmpty {
//                Text(viewModel.message)
//                    .foregroundColor(viewModel.signupSuccess ? .green : .red)
//                    .padding(.top, 10)
//            }
//            
//            Button(action: {
//                Task {
//                    isLoading = true
//                    await viewModel.signUp(
//                        userId: viewModel.userId,
//                        userPswd: viewModel.userPswd,
//                        userName: viewModel.userName,
//                        userMail: viewModel.userMail
//                    )
//                    isLoading = false
//                    
//                    if viewModel.signupSuccess {
//                        print("signupSuccess true → 메인 이동 시도")
//                        path.removeLast(path.count)
//                        path.append(Page.main)
//                    } else {
//                        print("signupSuccess false → 이동 안 함")
//                    }
//                    
//                }
//            }) {
//                if isLoading {
//                    ProgressView()
//                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
//                        .frame(maxWidth: .infinity)
//                        .padding()
//                        .background(Color.orange)
//                        .cornerRadius(14)
//                        .padding(.horizontal)
//                } else {
//                    Text("가입하기")
//                        .font(.headline)
//                        .foregroundColor(.primary)
//                        .frame(maxWidth: .infinity)
//                        .padding()
//                        .background(Color(.main))
//                        .cornerRadius(14)
//                        .padding(.horizontal)
//                }
//            }
//            .padding(.top, 10)
//            
//            Spacer()
//            
//            Button(action: {
//                dismiss() // 로그인 화면으로 돌아가기
//            }) {
//                Text("이미 계정이 있으신가요? 로그인")
//                    .foregroundColor(.gray)
//                    .font(.footnote)
//            }
//            .padding(.bottom, 40)
//        }
//        .navigationBarBackButtonHidden(false)
//        
//    }
//
//}


import SwiftUI

struct SignupView: View {
    @StateObject var viewModel = SignupViewModel()
    @State private var isLoading = false
    @State private var showEmailVerification = false
    
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        VStack(spacing: 20) {
            Text("회원가입")
                .font(.largeTitle)
                .fontWeight(.bold)
                .padding(.top, 50)
            
            if !showEmailVerification {
                // 회원정보 입력 화면
                signupFormView
            } else {
                // 이메일 인증 화면
                emailVerificationView
            }
        }
        .navigationBarBackButtonHidden(false)
    }
    
    // 회원정보 입력 폼
    private var signupFormView: some View {
        VStack(spacing: 20) {
            Group {
                TextField("이메일", text: $viewModel.userMail)
                    .padding()
                    .autocapitalization(.none)
                    .keyboardType(.emailAddress)
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                
                TextField("아이디", text: $viewModel.userId)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled(true)
                
                TextField("이름", text: $viewModel.userName)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                    .textInputAutocapitalization(.never)
                
                SecureField("비밀번호", text: $viewModel.userPswd)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)

                SecureField("비밀번호 확인", text: $viewModel.userPswdConfirm)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
            }
            .padding(.horizontal)
            
            if !viewModel.message.isEmpty {
                Text(viewModel.message)
                    .foregroundColor(.red)
                    .padding(.top, 10)
            }
            
            Button(action: {
                Task {
                    isLoading = true
                    let success = await viewModel.sendVerificationEmail()
                    isLoading = false
                    
                    if success {
                        showEmailVerification = true
                    }
                }
            }) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.orange)
                        .cornerRadius(14)
                        .padding(.horizontal)
                } else {
                    Text("다음")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.orange)
                        .cornerRadius(14)
                        .padding(.horizontal)
                }
            }
            .padding(.top, 10)
            
            Spacer()
            
            Button(action: {
                dismiss()
            }) {
                Text("이미 계정이 있으신가요? 로그인")
                    .foregroundColor(.gray)
                    .font(.footnote)
            }
            .padding(.bottom, 40)
        }
    }
    
    // 이메일 인증 화면
    private var emailVerificationView: some View {
        VStack(spacing: 20) {
            Image(systemName: "envelope.badge")
                .font(.system(size: 60))
                .foregroundColor(.orange)
                .padding(.top, 30)
            
            Text("이메일 인증")
                .font(.title2)
                .fontWeight(.bold)
            
            Text("\(viewModel.userMail)로\n인증 메일을 발송했습니다.")
                .multilineTextAlignment(.center)
                .foregroundColor(.gray)
                .padding(.horizontal)
            
            TextField("인증 코드 입력", text: $viewModel.verificationCode)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
                .padding(.horizontal)
                .keyboardType(.numberPad)
                .padding(.top, 20)
            
            if !viewModel.message.isEmpty {
                Text(viewModel.message)
                    .foregroundColor(viewModel.signupSuccess ? .green : .red)
                    .padding(.top, 10)
            }
            
            Button(action: {
                Task {
                    isLoading = true
                    await viewModel.verifyEmailAndSignup()
                    isLoading = false
                    
                    if viewModel.signupSuccess {
                        print("회원가입 성공 → 로그인 화면으로 이동")
                        dismiss()
                    }
                }
            }) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.orange)
                        .cornerRadius(14)
                        .padding(.horizontal)
                } else {
                    Text("인증 완료")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.orange)
                        .cornerRadius(14)
                        .padding(.horizontal)
                }
            }
            .padding(.top, 10)
            
            Button(action: {
                Task {
                    await viewModel.resendVerificationEmail()
                }
            }) {
                Text("인증 메일 재발송")
                    .foregroundColor(.orange)
                    .font(.footnote)
            }
            .padding(.top, 10)
            
            Spacer()
        }
    }
}
