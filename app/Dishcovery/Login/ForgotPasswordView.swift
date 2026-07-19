import SwiftUI

@available(iOS 16.0, *)
struct ForgotPasswordView: View {
    @StateObject private var viewModel = ForgotPasswordViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("가입한 이메일로 인증코드를 보내드려요")
                .font(.subheadline)
                .foregroundColor(.secondary)

            TextField("이메일", text: $viewModel.userMail)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled(true)
                .keyboardType(.emailAddress)
                .disabled(viewModel.showCodeField)

            if viewModel.showCodeField {
                TextField("인증코드", text: $viewModel.code)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)

                Button {
                    viewModel.sendCode()
                } label: {
                    Text("인증코드 재발송")
                        .foregroundColor(.orange)
                        .font(.footnote)
                }
            }

            Button {
                if viewModel.showCodeField {
                    viewModel.verifyCode()
                } else {
                    viewModel.sendCode()
                }
            } label: {
                Text(viewModel.showCodeField ? "인증하기" : "인증코드 보내기")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .disabled(viewModel.showCodeField ? viewModel.code.isEmpty : viewModel.userMail.isEmpty)

            Spacer()
        }
        .padding()
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast)
        .onChange(of: viewModel.didVerify) { success in
            if success {
                path.append(Page.resetPassword(userMail: viewModel.userMail, code: viewModel.code))
            }
        }
    }
}
