# import cv2

# img = cv2.imread('Screenshot (240).png')

# print(img.shape)

# gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
# cv2.imshow('window', img)
# cv2.waitKey(0)
# cv2.destroyAllWindows

import cv2

cap = cv2.VideoCapture(0) # 0 is the default camera

live = True

while live:
    ret, frame = cap.read()

    if not ret:
        break

    h, w = frame.shape[:2]

    # draw a box in the center, roughly where a face might be
    cv2.rectangle(frame, (w//2 - 100, h//2 - 100), (w//2 + 100, h//2 + 100), (0, 255, 0), 2)
    cv2.putText(frame, "Face goes here", (w//2-100, h//2-110),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

    #show
    cv2.imshow('Webcam', frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()