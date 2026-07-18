import SwiftUI

@available(iOS 16.0, *)
struct MyPageView: View {
    @StateObject private var viewModel = MyPageViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {

                // 프로필 카드
                VStack(spacing: 10) {
                    Image(systemName: "person.circle.fill")
                        .resizable()
                        .frame(width: 80, height: 80)
                        .foregroundColor(.orange)

                    if let profile = viewModel.profile {
                        Text(profile.userId)
                            .font(.headline)
                        Text(profile.userMail)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text("포인트 \(profile.pointBalance)P")
                            .font(.subheadline)
                            .foregroundColor(.orange)
                    }

                    HStack(spacing: 12) {
                        Button {
                            path.append(Page.changeNickname)
                        } label: {
                            Text("닉네임 변경")
                                .font(.subheadline)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }

                        Button {
                            path.append(Page.withdraw)
                        } label: {
                            Text("회원 탈퇴")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 4)
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(15)

                // 내가 등록한 레시피
                VStack(alignment: .leading, spacing: 10) {
                    Text("내가 등록한 레시피")
                        .font(.headline)

                    if viewModel.myRecipes.isEmpty {
                        Text("아직 등록한 레시피가 없어요.")
                            .foregroundColor(.secondary)
                    } else {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(viewModel.myRecipes) { recipe in
                                    Button {
                                        path.append(Page.recipeDetail(recipe: recipe))
                                    } label: {
                                        RecipeCardView(recipe: recipe)
                                            .frame(width: 160)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                        }
                    }
                }

                // 추천 유저
                VStack(alignment: .leading, spacing: 10) {
                    Text("추천 유저")
                        .font(.headline)

                    if viewModel.recommendedUsers.isEmpty {
                        Text("추천할 유저가 없어요.")
                            .foregroundColor(.secondary)
                    } else {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 16) {
                                ForEach(viewModel.recommendedUsers) { user in
                                    Button {
                                        path.append(Page.publicProfile(userId: user.userId))
                                    } label: {
                                        VStack(spacing: 6) {
                                            Image(systemName: "person.circle.fill")
                                                .resizable()
                                                .frame(width: 56, height: 56)
                                                .foregroundColor(.orange)
                                            Text(user.userName)
                                                .font(.caption)
                                                .foregroundColor(.primary)
                                                .lineLimit(1)
                                        }
                                        .frame(width: 80)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                        }
                    }
                }
            }
            .padding()
        }
        .task {
            await viewModel.load()
        }
    }
}
