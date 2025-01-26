import { Box } from "@mui/material";
import { ROLE, useProtectedResource } from "../../authUtils.ts";
import VideoContainer from "../streaming/videoContainer.tsx";

const StreamPreviewPage = function () {
  // redirect
  useProtectedResource(ROLE.USER);
  return (
    <Box>
      <VideoContainer />
    </Box>
  );
};

export default StreamPreviewPage;
