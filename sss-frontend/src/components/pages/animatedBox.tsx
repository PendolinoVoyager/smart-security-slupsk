import { useFrame } from "@react-three/fiber";
import { useRef } from "react";

export default function AnimatedBox() {
  const myMesh = useRef();
  useFrame(({ clock }) => {
    myMesh.current.rotation.x = clock.getElapsedTime();
    myMesh.current.rotation.y = clock.getElapsedTime();
  });
  return (
    <mesh ref={myMesh}>
      <torusGeometry />
      <meshToonMaterial />
    </mesh>
  );
}
