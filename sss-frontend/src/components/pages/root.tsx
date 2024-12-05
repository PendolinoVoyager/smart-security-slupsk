import { FC } from "react";
import { Outlet } from "react-router-dom";
import MainNav from "../nav/mainNav";
// import { Canvas } from "@react-three/fiber";
// import AnimatedBox from "./animatedBox";

const Root: FC = function () {

  return (
    <>
      <MainNav />
      {/*<Canvas*/}
      {/*  style={{*/}
      {/*    maxHeight: "99vh",*/}
      {/*    maxWidth: "99vw",*/}
      {/*    position: "absolute",*/}
      {/*    zIndex: "-10",*/}
      {/*  }}*/}
      {/*  shadows={true}*/}
      {/*>*/}
      {/*  <ambientLight intensity={0.1} />*/}
      {/*  <directionalLight color="red" position={[0, 0, 5]} />*/}
      {/*  <AnimatedBox />*/}
      {/*</Canvas>*/}
      <Outlet />
    </>
  );
};
export default Root;
