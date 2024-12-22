import { FC } from "react";
import { Outlet } from "react-router-dom";
import MainNav from "../nav/mainNav";
import VideoContainer from "../streaming/videoContainer";
import FlashMessages from "../flash/flashMessages";

// import { Canvas } from "@react-three/fiber";
// import AnimatedBox from "./animatedBox";

const Root: FC = function () {
  return (
    <>
      <FlashMessages />
      <MainNav />

      <VideoContainer />
      <Outlet />
    </>
  );
};
export default Root;
