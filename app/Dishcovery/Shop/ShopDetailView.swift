import SwiftUI

@available(iOS 16.0, *)
struct ShopDetailView: View {
    let productId: String
    @StateObject private var viewModel = ShopDetailViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if let product = viewModel.product {
                    Rectangle()
                        .fill(Color(.secondarySystemBackground))
                        .frame(height: 220)
                        .frame(maxWidth: .infinity)
                        .overlay(
                            Group {
                                if let path = product.mainImage {
                                    AsyncImage(
                                        url: URL(string: (API.baseURL) + path)
                                    ) { image in
                                        image.resizable().scaledToFill()
                                    } placeholder: {
                                        Color.clear
                                    }
                                }
                            }
                        )
                        .clipped()
                        .cornerRadius(12)

                    Text(product.productName)
                        .font(.title)
                        .fontWeight(.bold)

                    HStack {
                        Text("\(product.productPoint)P")
                            .font(.title3)
                            .fontWeight(.semibold)
                            .foregroundColor(.orange)
                        Spacer()
                        Text("내 포인트 \(viewModel.myPoint)P")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    Button {
                        viewModel.purchase(productId: productId)
                    } label: {
                        Text("구매하기")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.orange)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }

                    Divider()

                    // 리뷰
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text("리뷰 \(viewModel.reviews.count)개")
                                .font(.headline)
                            if !viewModel.reviews.isEmpty {
                                Text(String(format: "평균 %.1f", viewModel.averageRating))
                                    .font(.subheadline)
                                    .foregroundColor(.orange)
                            }
                        }

                        if viewModel.hasPurchased {
                            Picker("별점", selection: $viewModel.reviewRating) {
                                ForEach(1...5, id: \.self) { star in
                                    Text("\(star)점").tag(star)
                                }
                            }
                            .pickerStyle(.segmented)

                            TextField("리뷰를 남겨주세요", text: $viewModel.reviewContent)
                                .textFieldStyle(.roundedBorder)

                            Button {
                                viewModel.submitReview(productId: productId)
                            } label: {
                                Text("리뷰 등록")
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 8)
                                    .background(Color.orange.opacity(0.15))
                                    .foregroundColor(.orange)
                                    .cornerRadius(8)
                            }
                            .disabled(viewModel.reviewContent.isEmpty)
                        } else {
                            Text("구매한 상품만 리뷰를 작성할 수 있어요.")
                                .font(.footnote)
                                .foregroundColor(.secondary)
                        }

                        ForEach(viewModel.reviews) { review in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(String(repeating: "★", count: review.rating))
                                        .foregroundColor(.orange)
                                    Text(review.userId)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                Text(review.content)
                                    .font(.body)
                            }
                            .padding(.vertical, 4)
                        }
                    }

                    Divider()

                    // 문의
                    VStack(alignment: .leading, spacing: 10) {
                        Text("문의 \(viewModel.inquiries.count)개")
                            .font(.headline)

                        TextField("궁금한 점을 문의해주세요", text: $viewModel.inquiryContent)
                            .textFieldStyle(.roundedBorder)

                        Button {
                            viewModel.submitInquiry(productId: productId)
                        } label: {
                            Text("문의 등록")
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                                .background(Color.orange.opacity(0.15))
                                .foregroundColor(.orange)
                                .cornerRadius(8)
                        }
                        .disabled(viewModel.inquiryContent.isEmpty)

                        ForEach(viewModel.inquiries) { inquiry in
                            VStack(alignment: .leading, spacing: 4) {
                                Text(inquiry.userId)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text(inquiry.content)
                                    .font(.body)
                            }
                            .padding(.vertical, 4)
                        }
                    }

                    if !viewModel.recommended.isEmpty {
                        Divider()
                        VStack(alignment: .leading, spacing: 10) {
                            Text("추천 상품")
                                .font(.headline)
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(viewModel.recommended) { p in
                                        VStack {
                                            Text(p.productName)
                                                .font(.caption)
                                                .lineLimit(1)
                                            Text("\(p.productPoint)P")
                                                .font(.caption2)
                                                .foregroundColor(.orange)
                                        }
                                        .frame(width: 100)
                                        .padding(8)
                                        .background(Color(.secondarySystemBackground))
                                        .cornerRadius(10)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ProgressView()
                        .padding(.top, 60)
                }
            }
            .padding()
        }
        .navigationTitle("상품 상세")
        .navigationBarTitleDisplayMode(.inline)
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast)
        .task {
            await viewModel.load(productId: productId)
        }
    }
}
