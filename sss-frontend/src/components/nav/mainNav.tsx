import { useContext, useState } from "react";
import NavItem from "./navItem";

import { AuthContext } from "../../store/authStore";
import {
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  Paper,
  Theme,
  useMediaQuery,
} from "@mui/material";

import { Menu, Person } from "@mui/icons-material";
import { ROLE } from "../../authUtils";

export default function MainNav() {
  const { loggedIn, email, role } = useContext(AuthContext);
  const [isDrawerOpen, setDrawerOpen] = useState(false);
  const isSmallScreen = useMediaQuery((theme: Theme) =>
    theme.breakpoints.down("md")
  );

  const renderNavItems = () => (
    <>
      <ListItem>
        <NavItem href={"/"}>Home</NavItem>
      </ListItem>
      {role === ROLE.ADMIN && (
        <ListItem>
          <NavItem href="/admin">Admin panel</NavItem>
        </ListItem>
      )}
      {loggedIn ? (
        <>
          <ListItem>
            <NavItem href={"/devices"}>Devices</NavItem>
          </ListItem>
          <ListItem>
            <span>{email}</span>
            <div className="divider"></div>
            <NavItem href={"/logout"}>Logout</NavItem>
          </ListItem>
        </>
      ) : (
        <ListItem>
          <NavItem href={"/login"}>
            <Person />
            <br />
            Login
          </NavItem>
        </ListItem>
      )}
    </>
  );

  return (
    <Paper elevation={1} style={{ marginTop: 0 }}>
      {isSmallScreen ? (
        <Box display={"flex"} alignItems={"center"}>
          <IconButton
            onClick={() => setDrawerOpen(!isDrawerOpen)}
            style={{ float: "left" }}
          >
            <Menu />
          </IconButton>
          <Drawer
            anchor="left"
            open={isDrawerOpen}
            onClose={() => setDrawerOpen(false)}
          >
            <List>{renderNavItems()}</List>
          </Drawer>
        </Box>
      ) : (
        <List
          style={{
            display: "flex",
            flexDirection: "row",
            padding: 0,
            justifyContent: "center",
          }}
        >
          {renderNavItems()}
        </List>
      )}
    </Paper>
  );
}
