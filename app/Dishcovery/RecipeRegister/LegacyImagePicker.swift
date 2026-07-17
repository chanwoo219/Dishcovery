import SwiftUI
import UIKit

// A simple UIImagePickerController wrapper for SwiftUI
// Works on iOS 14+ and avoids newer PhotosPicker APIs if you need broader compatibility.
public struct LegacyImagePicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    var allowsEditing: Bool = false
    var sourceType: UIImagePickerController.SourceType = .photoLibrary

    public init(image: Binding<UIImage?>, allowsEditing: Bool = false, sourceType: UIImagePickerController.SourceType = .photoLibrary) {
        self._image = image
        self.allowsEditing = allowsEditing
        self.sourceType = sourceType
    }

    public func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = sourceType
        picker.allowsEditing = allowsEditing
        return picker
    }

    public func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {
        // No-op
    }

    public func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    public final class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: LegacyImagePicker

        init(_ parent: LegacyImagePicker) {
            self.parent = parent
        }

        public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            let key: UIImagePickerController.InfoKey = parent.allowsEditing ? .editedImage : .originalImage
            if let selected = info[key] as? UIImage {
                parent.image = selected
            }
            picker.dismiss(animated: true)
        }

        public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            picker.dismiss(animated: true)
        }
    }
}
