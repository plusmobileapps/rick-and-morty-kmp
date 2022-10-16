//
// Created by Andrew Steinmetz on 10/16/22.
//

import SwiftUI

struct LabelWithTextView : View {
    let label: String
    let text: String

    var body: some View {
        HStack {
            Text(label)
                    .font(.system(size: 20, weight: .bold, design: .default))
            Spacer().frame(width: 16)
            Text(text)
                    .font(.system(size: 20, weight: .medium, design: .default))
        }
    }
}