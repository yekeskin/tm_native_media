import UIKit

extension UIImage {
    func rotate(degrees: Float) -> UIImage? {
        let radians = degrees * (.pi/180);
        var newSize = CGRect(origin: CGPoint.zero, size: self.size).applying(CGAffineTransform(rotationAngle: CGFloat(radians))).size
        // Trim off the extremely small float value to prevent core graphics from rounding it up
        newSize.width = floor(newSize.width)
        newSize.height = floor(newSize.height)

        UIGraphicsBeginImageContextWithOptions(newSize, false, self.scale)
        let context = UIGraphicsGetCurrentContext()!

        // Move origin to middle
        context.translateBy(x: newSize.width/2, y: newSize.height/2)
        // Rotate around middle
        context.rotate(by: CGFloat(radians))
        // Draw the image at its center
        self.draw(in: CGRect(x: -self.size.width/2, y: -self.size.height/2, width: self.size.width, height: self.size.height))

        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage
    }
    
    func resizeImage(size: CGSize) -> UIImage? {
        UIGraphicsBeginImageContext(size)
        self.draw(in: CGRect(origin: CGPoint.zero, size: size))
        let resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return resizedImage
    }
    
    func flipHorizontal() -> UIImage? {
        UIGraphicsBeginImageContextWithOptions(self.size, false, self.scale)
        let context = UIGraphicsGetCurrentContext()!
        
        context.scaleBy(x: -1.0, y: 1.0)
        context.translateBy(x: -self.size.width, y: 0.0)
        self.draw(in: CGRect(origin: .zero, size: self.size))

        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage
    }
    
    func flipVertical() -> UIImage? {
        UIGraphicsBeginImageContextWithOptions(self.size, false, self.scale)
        let context = UIGraphicsGetCurrentContext()!
        
        context.scaleBy(x: 1.0, y: -1.0)
        context.translateBy(x: 0.0, y: -self.size.height)
        self.draw(in: CGRect(origin: .zero, size: self.size))

        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage
    }
}

public class ImageTools {
    public static func compressImage(image: UIImage, quality: Int) -> Data? {
        return image.jpegData(compressionQuality: CGFloat(Float(quality) / 100.0))
    }
    
    public static func scaleImage(inputPath: String, height: CGFloat, width: CGFloat) -> UIImage? {
        let unscaledImage: UIImage = UIImage(contentsOfFile: inputPath)!
        
        var outWidth = width
        var outHeight = height
        
        if(width == 0 || height == 0 || (width > unscaledImage.size.width && height > unscaledImage.size.height)) {
            outWidth = unscaledImage.size.width;
            outHeight = unscaledImage.size.height;
        }
        let newSize = calculateAspectRatio(inWidth: unscaledImage.size.width, inHeight: unscaledImage.size.height, outWidth: outWidth, outHeight: outHeight)
        
        return unscaledImage.resizeImage(size: newSize)
    }
    
    public static func cropImage(inputImage: UIImage, x: CGFloat, y: CGFloat, width: CGFloat, height: CGFloat) -> UIImage? {
        let cropRect = CGRect(x: x, y: y, width: width, height: height);
        let cropped = inputImage.cgImage?.cropping(to: cropRect);
        if(cropped == nil) {
            return nil;
        } else {
            return UIImage(cgImage: cropped!)
        }
    }
    
    public static func rotateImage(inputImage: UIImage, degrees: Int) -> UIImage? {
        return inputImage.rotate(degrees: Float(degrees))
    }
    
    public static func flipImageHorizontal(inputImage: UIImage) -> UIImage? {
        return inputImage.flipHorizontal()
    }
    
    public static func flipImageVertical(inputImage: UIImage) -> UIImage? {
        return inputImage.flipVertical()
    }
    
    static func calculateAspectRatio(inWidth: CGFloat, inHeight: CGFloat, outWidth: CGFloat, outHeight: CGFloat) -> CGSize {
        var newWidth = outWidth;
        var newHeight = outHeight;

        if (newWidth <= 0 && newHeight <= 0) {
            newWidth = inWidth;
            newHeight = inHeight;
        } else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * inHeight) / inWidth;
        } else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * inWidth) / inHeight;
        } else {
            let newRatio = newWidth / newHeight;
            let origRatio = inWidth / inHeight;
            if (origRatio > newRatio) {
                newHeight = (newWidth * inHeight) / inWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * inWidth) / inHeight;
            }
        }

        return CGSize(
            width: newWidth,
            height: newHeight
        )
    }
}
