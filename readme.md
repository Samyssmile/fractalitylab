# FractalityLab

## Overview
FractalityLab is a powerful Java-based tool for generating datasets of fractal images, suitable for machine learning applications. This utility is part of the `edux` machine learning library suite and is tailored for creating intricate datasets that can be used to train and improve machine learning models.

## Usage
```bash
# Create 100 Images fo each fractal class with 256x256 pixel size. Quality 76 (100 is perfect, 0 is worst), trainTestRatio 0.80 means, 80% of the images will be used for training and 20% for testing.
java -jar FractalityLab-1.3.jar --number 100 --size 256 --quality 74 --trainTestRatio 0.80 
```

### Check out the `edux` library here:
[EDUX - Java Machine Learning Library](https://github.com/Samyssmile/edux)

### Examples
![alt text](https://hc-linux.eu/edux/0a7cdc9f-f3e7-4d7f-9238-ff7d1a487a78.png)
![alt text](https://hc-linux.eu/edux/0ae58db5-a749-4416-b623-aadfb4f62c22.png)
![alt text](https://hc-linux.eu/edux/0b69dff9-68fd-4ff2-bea4-810a452b71cc.png)
![alt text](https://hc-linux.eu/edux/2d483ca2-3579-428e-8dcc-3034208a801c.png)
![alt text](https://hc-linux.eu/edux/9b529a2f-5e63-4dd9-939c-12a09dec41e4.png)
![alt text](https://hc-linux.eu/edux/e4c215af-3f02-4250-b1af-7d23b52dc15f.png)

### Downloads
You can download pre generated datasets from the following links:

- [Small - 3000 Images with 64x64 size](https://hc-linux.eu/edux/fractality-S.zip)
- [Medium - 3000 Images with 512x512 size](https://hc-linux.eu/edux/fractality-L.zip)
- [Large - 30 Images with 4000x4000 size](https://hc-linux.eu/edux/fractality-XL.zip)

## Features
- Multiple fractal generators including Mandelbrot, Julia, Burning Ship, Newton, SierpinskiGasket and more.
- Configurable image size and quantity.
- .png Images with CSV output of generated fractal data.
- Easy to use command line interface.


This command will create a folder dataset with images and a csv file containing the labels you need for training your machine learning model.
```
├───images.csv
├───dataset
    ├───class
       ├───burningship
       ├───julia
       ├───mandelbrot
       ├───newton
       ├───sierpinski_gasket
       └───tricorn
```



## Contributing
We welcome contributions. If you have suggestions or contributions, please fork the repository and submit a pull request.
