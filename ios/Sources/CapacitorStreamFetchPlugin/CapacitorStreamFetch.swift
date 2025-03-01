import Foundation

@objc public class CapacitorStreamFetch: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
