import SwiftUI

@available(iOS 16.0, *)
struct ChangeNicknameView: View {
    @StateObject private var viewModel = ChangeNicknameViewModel()
    @EnvironmentObject var appState: AppState
    @Binding var path: NavigationPath

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("닉네임 (최대 6자)")
                .font(.subheadline)
                .foregroundColor(.secondary)

            TextField("닉네임", text: $viewModel.userName)
                .textFieldStyle(.roundedBorder)
                .onChange(of: viewModel.userName) { newValue in
                    if newValue.count > 6 {
                        viewModel.userName = String(newValue.prefix(6))
                    }
                }

            Button {
                viewModel.save()
            } label: {
                Text("저장")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .disabled(viewModel.userName.isEmpty)

            Spacer()
        }
        .padding()
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast)
        .onChange(of: viewModel.didSucceed) { success in
            if success {
                appState.username = viewModel.userName
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                    if !path.isEmpty { path.removeLast() }
                }
            }
        }
    }
}
