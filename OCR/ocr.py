from PIL import Image 
import pytesseract as pt 
import os

def main(): 
    path ="resized/"
    outPath ="OCR.txt"
    lst = os.listdir(path)
    lst.sort()

    for file in lst: 
        if not file.lower().endswith(('.png', '.jpg', '.jpeg', '.tiff', '.bmp', '.gif')):
            continue
        inputPath = os.path.join(path, file)
        img = Image.open(inputPath)
        text = pt.image_to_string(img, lang ="eng") 
  
        file1 = open(outPath, "a+")
        file1.write(file+"\n") 
        file1.write(text+"\n") 
        file1.close()

        print('Page #{} converted'.format(file))  

if __name__ == '__main__': 
	main()