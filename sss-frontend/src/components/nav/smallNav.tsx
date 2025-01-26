import React from "react";
import { Person } from "@mui/icons-material";
import {
  ListItem,
  Box,
  IconButton,
  Popover,
  Menu,
  Drawer,
  List,
} from "@mui/material";
import { ROLE } from "../../authUtils";
import LoginForm from "../login/loginForm";
import NavItem from "./navItem";

interface SmallNavProps {
  loggedIn: boolean;
  role: ROLE;
  email: string;
}

const SmallNav: React.FC<SmallNavProps> = function ({ loggedIn, role, email }) {
  // the actual nav items
  // rest of the code handles how its rendered
  const renderNavItems = () => (
    <>
      <ListItem>
        <NavItem href={"/"}>Home</NavItem>
      </ListItem>
      {role === ROLE.ADMIN && (
        <ListItem>
          <NavItem href="/admin">Admin Panel</NavItem>
        </ListItem>
      )}
      {loggedIn && (
        <>
          <ListItem>
            <NavItem href={"/devices"}>Devices</NavItem>
          </ListItem>
          <ListItem>
            <NavItem href={"/notifications"}>Notifications</NavItem>
          </ListItem>
        </>
      )}
      {loggedIn ? (
        <ListItem>
          <Box display="flex" alignItems="center" gap={2}>
            <span>{email}</span>
            <NavItem href="#" onClick={logout}>
              Logout
            </NavItem>
          </Box>
        </ListItem>
      ) : (
        <ListItem>
          <IconButton onClick={handleLoginClick}>
            <Person fontSize="small" />
          </IconButton>
          <Popover
            open={Boolean(anchorEl)}
            anchorEl={anchorEl}
            onClose={handleClosePopover}
            anchorOrigin={{
              vertical: "bottom",
              horizontal: "right",
            }}
          >
            <LoginForm onLoginSuccess={handleClosePopover} />
          </Popover>
        </ListItem>
      )}
    </>
  );

  return (
    <Box display={"flex"} alignItems={"center"}>
      <IconButton
        onClick={() => setDrawerOpen(!isDrawerOpen)}
        sx={{ float: "left" }}
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
  );
};

export default SmallNav;
