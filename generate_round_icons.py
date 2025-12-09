import os
from PIL import Image, ImageOps, ImageDraw

def create_round_icon(source_path, size, output_path):
    # Open valid image
    img = Image.open(source_path).convert("RGBA")
    
    # Resize with high quality resampling
    img = img.resize((size, size), Image.Resampling.LANCZOS)
    
    # Create circular mask
    mask = Image.new('L', (size, size), 0)
    draw = ImageDraw.Draw(mask) 
    draw.ellipse((0, 0, size, size), fill=255)
    
    # Apply mask
    output = ImageOps.fit(img, (size, size), centering=(0.5, 0.5))
    output.putalpha(mask)
    
    # Ensure directory exists (it should, but safety first)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # Save as PNG
    output.save(output_path, "PNG")
    print(f"Generated: {output_path}")

source = "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
configs = [
    (48, "app/src/main/res/mipmap-mdpi/ic_launcher_round.png"),
    (72, "app/src/main/res/mipmap-hdpi/ic_launcher_round.png"),
    (96, "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png"),
    (144, "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png"),
    (192, "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png")
]

if not os.path.exists(source):
    print(f"Error: {source} not found!")
else:
    for size, path in configs:
        create_round_icon(source, size, path)
