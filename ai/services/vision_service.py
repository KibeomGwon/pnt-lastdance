from ultralytics import YOLO
from PIL import Image

class VisionService:
    def __init__(self, weights_path="./models/best.pt"):
        print(f"👁️ [VisionService] Loading YOLO (CPU) from {weights_path}...")
        # task='detect' 명시
        self.model = YOLO(weights_path, task='detect') 
        print("✅ [VisionService] Loaded!")

    def check_object(self, pil_image: Image.Image, target_name: str, conf_thres=0.25):
        results = self.model.predict(pil_image, device='cpu', conf=conf_thres, verbose=False)
        
        found = False
        max_conf = 0.0
        
        for result in results:
            for box in result.boxes:
                cls_id = int(box.cls[0])
                conf = float(box.conf[0])
                cls_name = self.model.names[cls_id]
                
                if cls_name == target_name:
                    found = True
                    max_conf = max(max_conf, conf)
        
        return ("O", max_conf) if found else ("X", 0.0)