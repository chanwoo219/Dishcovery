import SwiftUI

@available(iOS 16.0, *)
struct ResetPasswordView: View {
    let userMail: String
    @StateObject private var viewModel = ResetPasswordViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(userMail)
                .font(.subheadline)
                .foregroundColor(.secondary)

            TextField("6자리 인증코드", text: $viewModel.code)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.numberPad)

            SecureField("새 비밀번호", text: $viewModel.newPassword)
                .textFieldStyle(.roundedBorder)

            SecureField("비밀번호 확인", text: $viewModel.confirmPassword)
                .textFieldStyle(.roundedBorder)

            Button {
                viewModel.reset(userMail: userMail)
            } label: {
                Text("비밀번호 변경")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .disabled(viewModel.code.isEmpty || viewModel.newPassword.isEmpty || viewModel.confirmPassword.isEmpty)

            Spacer()
        }
        .padding()
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast)
        .onChange(of: viewModel.didSucceed) { success in
            if success {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
                    path.removeLast(path.count)
                }
            }
        }
    }
}
