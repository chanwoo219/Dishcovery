import Foundation
  

struct CodeVo: Codable, Identifiable {
    var id: String { code }
    
    let codeHead: String
    let code: String
    let codeName: String
    let parentCode: String?
    let codeSort: Int
}
