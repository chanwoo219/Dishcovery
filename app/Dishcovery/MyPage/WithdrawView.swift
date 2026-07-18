import SwiftUI

@available(iOS 16.0, *)
struct WithdrawView: View {
    @StateObject private var viewModel = WithdrawViewModel()
    @EnvironmentObject var appState: AppState
    @Binding var path: NavigationPath
    @State private var showConfirm = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("탈퇴 시 로그인이 더 이상 불가능합니다. 계속하려면 비밀번호를 입력해주세요.")
                .font(.subheadline)
                .foregroundColor(.secondary)

            SecureField("비밀번호", text: $viewModel.password)
                .textFieldStyle(.roundedBorder)

            Button {
                showConfirm = true
            } label: {
                Text("탈퇴하기")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .disabled(viewModel.password.isEmpty)
            .confirmationDialog(
                "정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
                isPresented: $showConfirm,
                titleVisibility: .visible
            ) {
                Button("탈퇴", role: .destructive) {
                    viewModel.withdraw()
                }
                Button("취소", role: .cancel) {}
            }

            Spacer()
        }
        .padding()
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast)
        .onChange(of: viewModel.didSucceed) { success in
            if success {
                appState.isLoggedIn = false
                appState.username = ""
                UserDefaults.standard.removeObject(forKey: "JWT_TOKEN")
                UserDefaults.standard.removeObject(forKey: "USERNAME")
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
                    path.removeLast(path.count)
                }
            }
        }
    }
}
