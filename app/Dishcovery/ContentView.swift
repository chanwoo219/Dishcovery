import SwiftUI

enum Page: Hashable {
    case signup
    case login
    case main
    case aiRecipe
    case recipeWrite
    case myRecipe
    case recipeDetail(recipe: Recipe)
    case shop
    case shopDetail(productId: String)
    case myPage
    case changeNickname
    case withdraw
    case publicProfile(userId: String)
    case forgotPassword
    case resetPassword(userMail: String)
}

@available(iOS 16.0, *)
struct ContentView: View {
    @StateObject var viewModel = RecipeViewModel()
    
    @State private var showMenu = false
    @State private var path = NavigationPath()
    @State private var searchText = ""

    @EnvironmentObject var appState: AppState

    private var filteredRecipes: [Recipe] {
        guard !searchText.isEmpty else { return viewModel.recipes }
        return viewModel.recipes.filter { $0.title.localizedCaseInsensitiveContains(searchText) }
    }

    var body: some View {
        NavigationStack(path: $path) {
            
            // Bridge a single selection binding for views that expect Binding<Page?>
            let selectedPage = Binding<Page?>(
                get: { nil },
                set: { newValue in
                    if let page = newValue {
                        path.append(page)
                    } else {
                        path.removeLast(path.count)
                    }
                }
            )
            
            ZStack {
                
                // MAIN CONTENT
                VStack(spacing: 0) {
                    // 상단 헤더
                    HStack {
                        // 햄버거 메뉴 버튼
                        Button(action: {
                            withAnimation {
                                showMenu.toggle()
                            }
                        }) {
                            VStack(spacing: 4) {
                                Rectangle().frame(width: 25, height: 3)
                                Rectangle().frame(width: 25, height: 3)
                                Rectangle().frame(width: 25, height: 3)
                            }
                            .foregroundColor(.orange)
                        }
                        
                        Text("Dishcovery")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                            .padding(.leading, 8)
                        
                        Spacer()
                        
                        if appState.isLoggedIn {
                            Button {
                                path.append(Page.myPage)
                            } label: {
                                AvatarView(imgPath: appState.userImgPath, size: 36)
                            }

                        } else {
                            NavigationLink(value: Page.login) {
                                Text("로그인")
                                    .font(.headline)
                                    .foregroundColor(.white)
                                    .padding(.vertical, 8)
                                    .padding(.horizontal, 16)
                                    .background(Color.orange)
                                    .cornerRadius(10)
                            }
                        }
                        
                    }
                    .padding(.horizontal)
                    .padding(.top, 16)
                    .padding(.bottom, 15)
                    
                    // 레시피 리스트
                    ScrollView {
                        if searchText.isEmpty == false && filteredRecipes.isEmpty {
                            Text("'\(searchText)' 검색 결과가 없어요.")
                                .foregroundColor(.secondary)
                                .padding(.top, 40)
                        } else {
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                                ForEach(filteredRecipes) { recipe in
                                    Button {
                                        path.append(Page.recipeDetail(recipe: recipe))
                                    } label: {
                                        RecipeCardView(recipe: recipe)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                            .padding(.horizontal)
                            .padding(.top)
                        }
                    }
                }
                .searchable(text: $searchText, prompt: "레시피 검색")
                .task {
                    await viewModel.fetchRecipes()
                    await loadMyAvatarIfNeeded()
                }
                .onChange(of: appState.isLoggedIn) { isLoggedIn in
                    if isLoggedIn {
                        Task { await loadMyAvatarIfNeeded() }
                    } else {
                        appState.userImgPath = nil
                    }
                }

                //사이드메뉴 수정
                SideMenuView(showMenu: $showMenu, path: $path)
            }
            
            
            // 페이지 이동 처리
            .navigationDestination(for: Page.self) { page in
                switch page {
                case .login:
                    LoginView(route: selectedPage, viewModel: LoginViewModel(appState: appState))
                        .navigationTitle("로그인")
                        .navigationBarTitleDisplayMode(.inline)
                case .signup:
                    SignupView()
                        .navigationTitle("회원가입")
                        .navigationBarTitleDisplayMode(.inline)
                case .recipeWrite:
                    RecipeRegisterView(path: $path)
                        .navigationTitle("레시피 등록")
                        .navigationBarTitleDisplayMode(.inline)
                    
                case .aiRecipe:
                    RecipeAiView()
                        .navigationTitle("AI 레시피추천")
                        .navigationBarTitleDisplayMode(.inline)
                case .myRecipe:
                    MyRecipeView(path: $path)
                        .navigationTitle("나의 레시피")
                        .navigationBarTitleDisplayMode(.inline)
                case .shop:
                    ShopListView(path: $path)
                        .navigationTitle("상점")
                        .navigationBarTitleDisplayMode(.inline)
                case .shopDetail(let productId):
                    ShopDetailView(productId: productId)
                case .myPage:
                    MyPageView(path: $path)
                        .navigationTitle("마이페이지")
                        .navigationBarTitleDisplayMode(.inline)
                case .changeNickname:
                    ChangeNicknameView(path: $path)
                        .navigationTitle("닉네임 변경")
                        .navigationBarTitleDisplayMode(.inline)
                case .withdraw:
                    WithdrawView(path: $path)
                        .navigationTitle("회원 탈퇴")
                        .navigationBarTitleDisplayMode(.inline)
                case .publicProfile(let userId):
                    PublicProfileView(userId: userId, path: $path)
                        .navigationTitle("프로필")
                        .navigationBarTitleDisplayMode(.inline)
                case .forgotPassword:
                    ForgotPasswordView(path: $path)
                        .navigationTitle("비밀번호 찾기")
                        .navigationBarTitleDisplayMode(.inline)
                case .resetPassword(let userMail):
                    ResetPasswordView(userMail: userMail, path: $path)
                        .navigationTitle("비밀번호 변경")
                        .navigationBarTitleDisplayMode(.inline)
                case .main:
                    EmptyView()
                        .onAppear { path.removeLast(path.count) }
                        .navigationTitle("메인")
                        .navigationBarTitleDisplayMode(.inline)
                case .recipeDetail(let recipe):
                    RecipeDetailView(recipe: recipe)
                }
            }
        }
    }

    private func loadMyAvatarIfNeeded() async {
        guard appState.isLoggedIn else { return }
        do {
            let profile = try await UserApiService.shared.fetchMyProfile()
            appState.userImgPath = profile.userImgPath
        } catch {
            print("🔴 [ContentView] 프로필 사진 불러오기 실패:", error.localizedDescription)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        if #available(iOS 16.0, *) {
            ContentView()
                .environmentObject(AppState())
        } else {
            // Fallback on earlier versions
        }
    }
}

