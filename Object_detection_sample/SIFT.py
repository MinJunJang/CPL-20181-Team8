
import numpy as np
import cv2

def featureMatching():
    img1=cv2.imread('tt1.jpg')
    img2=cv2.imread('tt2.jpg')
    res=None
    
    sift= cv2.xfeatures2d.SIFT_create()
    kp1,des1=sift.detectAndCompute(img1,None)
    kp2,des2=sift.detectAndCompute(img2,None)
    
    bf=cv2.BFMatcher(cv2.NORM_L2,crossCheck=True)
    matches=bf.match(des1,des2)
    
    matches=sorted(matches,key=lambda x:x.distance)
    res=cv2.drawMatches(img1,kp1,img2,kp2,matches[:30],res, flags=0)
    
    cv2.imshow('F_M',res)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    
featureMatching()
    
    