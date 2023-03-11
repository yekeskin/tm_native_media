import Flutter
import UIKit
import Foundation
import Photos


public class SwiftTMNativeMedia: NSObject, FlutterPlugin, FlutterStreamHandler {
    var transcodeEventSink: FlutterEventSink?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "tm_native_media", binaryMessenger: registrar.messenger())
        let transcodeEventChannel = FlutterEventChannel(name: "transcode_event_channel", binaryMessenger: registrar.messenger())
        let instance = SwiftTMNativeMedia()
        registrar.addMethodCallDelegate(instance, channel: channel)
      
        transcodeEventChannel.setStreamHandler(instance)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch(call.method) {
            case "compressImage":
                compressImage(call: call, result: result)
                break
            case "processImage":
                processImage(call: call, result: result)
                break
            case "addToGallery":
                addToGallery(call: call, result: result)
                break
            default:
                result(FlutterError.init())
                break
        }
    }
    
    func processImage(call: FlutterMethodCall, result: FlutterResult) {
        let args = call.arguments as? Dictionary<String, Any?>
        
        let inputPath = args?["input"] as? String ?? ""
        let outputPath = args?["output"] as? String ?? ""
        let quality = args?["quality"] as? Int ?? 0
        let cropX = CGFloat(args?["cropX"] as? Int ?? 0)
        let cropY = CGFloat(args?["cropY"] as? Int ?? 0)
        let cropWidth = CGFloat(args?["cropWidth"] as? Int ?? 0)
        let cropHeight = CGFloat(args?["cropHeight"] as? Int ?? 0)
        let verticalFlip = args?["verticalFlip"] as? Bool ?? false
        let horizontalFlip = args?["horizontalFlip"] as? Bool ?? false
        let rotate = args?["rotate"] as? Int ?? 0

        do {
            let inputImage = UIImage(contentsOfFile: inputPath)!
            let croppedImage = ImageTools.cropImage(inputImage: inputImage, x: cropX, y: cropY, width: cropWidth, height: cropHeight)
            let rotatedImage = ImageTools.rotateImage(inputImage: croppedImage!, degrees: rotate)
            var flippedImage = rotatedImage
            if(verticalFlip) {
                flippedImage = ImageTools.flipImageVertical(inputImage: flippedImage!)
            }
            if(horizontalFlip) {
                flippedImage = ImageTools.flipImageHorizontal(inputImage: flippedImage!)
            }
            if(flippedImage != nil) {
                let compressedImageData = ImageTools.compressImage(image: flippedImage!, quality: quality)
                try compressedImageData!.write(to: URL(fileURLWithPath: outputPath), options: .atomic)
                result(outputPath)
            }
        } catch {
            result(FlutterError.init(
                code: "Exception",
                message: error.localizedDescription,
                details: nil)
            )
        }
    }
    
    func compressImage(call: FlutterMethodCall, result: FlutterResult) {
        let args = call.arguments as? Dictionary<String, Any?>
        
        let inputPath = args?["input"] as? String ?? ""
        let outputPath = args?["output"] as? String ?? ""
        let width = CGFloat(args?["width"] as? Int ?? 0)
        let height = CGFloat(args?["height"] as? Int ?? 0)
        let quality = args?["quality"] as? Int ?? 0

        do {
            let scaledImage = ImageTools.scaleImage(inputPath: inputPath, height: height, width: width)
            if(scaledImage != nil) {
                let compressedImageData = ImageTools.compressImage(image: scaledImage!, quality: quality)
                try compressedImageData!.write(to: URL(fileURLWithPath: outputPath), options: .atomic)
                result(outputPath)
            }
        } catch {
            result(FlutterError.init(
                code: "Exception",
                message: error.localizedDescription,
                details: nil)
            )
        }
    }
    
    public func onListen(withArguments arguments: Any?, eventSink: @escaping FlutterEventSink) -> FlutterError? {
            transcodeEventSink = eventSink
            return nil
     }

    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        transcodeEventSink = nil
        return nil
    }

    public func addToGallery(call: FlutterMethodCall, result:@escaping FlutterResult) {
        let args = call.arguments as? Dictionary<String, Any?>
        
        let fromPath = args?["path"] as? String ?? ""
        let isVideo = args?["isVideo"] as? Bool ?? false

        var albumName = "Teknorota Images"
        if(isVideo) {
            albumName = "Teknorota Videos"
        }
        let fromURL = URL(fileURLWithPath: fromPath)

        guard let album = findOrCreateAlbum(named: albumName) else {
            result(false)
            return;
        }

        DispatchQueue.global().async {
            PHPhotoLibrary.shared().performChanges({
                var assetReq: PHAssetChangeRequest?
                
                if isVideo {
                    assetReq = PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: fromURL)
                } else {
                    assetReq = PHAssetChangeRequest.creationRequestForAssetFromImage(atFileURL: fromURL)
                }
                
                if let asset = assetReq?.placeholderForCreatedAsset {
                    let request = PHAssetCollectionChangeRequest(for: album)
                    request?.addAssets([asset] as NSArray)
                }
                
            }) { (done, err) in
                if err != nil {
                    result(false)
                } else {
                    result(true)
                }
            }
        }
    }

    func findOrCreateAlbum(named albumName: String) -> PHAssetCollection? {
        if let album = findAlbum(named: albumName) {
            return album
        } else {
            do {
                try PHPhotoLibrary.shared().performChangesAndWait({
                    PHAssetCollectionChangeRequest.creationRequestForAssetCollection(withTitle: albumName)
                })
            } catch {
                print("Problem finding/creating album: ".appending(albumName))
                print(error)
            }
            
            return findAlbum(named: albumName)
        }
    }
    
    func findAlbum(named albumName: String) -> PHAssetCollection? {
        let options = PHFetchOptions()
        options.predicate = NSPredicate(format: "title = %@", albumName)
        let findAlbumResult = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: options)
        return findAlbumResult.firstObject
    }
}
