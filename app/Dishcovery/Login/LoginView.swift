import SwiftUI

struct LoginView: View {
    
    @State private var userId: String = ""
    @State private var userPswd: String = ""
    @State private var goToSignUp = false
    
    @Binding var route: Page?
    @ObservedObject var viewModel: LoginViewModel


    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            Text("로그인")
                .font(.largeTitle)
                .bold()
                .padding(.top, 20)

            TextField("아이디", text: $viewModel.userId)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled(true)
            SecureField("비밀번호", text: $viewModel.userPswd)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
            
            Spacer()
            
            Button(action: {
                DispatchQueue.main.async {
                    goToSignUp = true
                }
            }) {
                Text("회원가입하시겠습니까?")
                    .font(.footnote)
                    .foregroundColor(.gray)
                    .padding(.top, 10)
            }
            NavigationLink(destination: SignupView(), isActive: $goToSignUp) {
                EmptyView()
            }
            
            Button(action: {
               Task {
                   await viewModel.login()
                   if viewModel.loginSuccess {
                       route = nil
                   }
               }
           }) {
               Text("로그인")
                   .font(.headline)
                   .foregroundColor(.white)
                   .frame(maxWidth: .infinity)
                   .padding()
                   .background(Color.orange)
                   .cornerRadius(10)
           }
           .padding(.top, 10)

           Text(viewModel.message)
               .foregroundColor(viewModel.loginSuccess ? .green : .red)
               .padding(.top, 10)
       }
       .padding()
    }
}
