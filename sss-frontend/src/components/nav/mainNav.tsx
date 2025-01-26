import React, { useContext, useState } from "react";
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
  Popover,
} from "@mui/material";
import { Menu, Person } from "@mui/icons-material";
import { ROLE } from "../../authUtils";
import LoginForm from "../../components/login/loginForm.tsx";

export default function MainNav() {
  const { loggedIn, email, role, logout } = useContext(AuthContext);
  const [isDrawerOpen, setDrawerOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);
  const isSmallScreen = useMediaQuery((theme: Theme) =>
    theme.breakpoints.down("md")
  );

  const handleLoginClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClosePopover = () => {
    setAnchorEl(null);
  };
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
    <Paper elevation={1} sx={{ marginTop: 0 }}>
      {isSmallScreen ? (
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
      ) : (
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="center"
          padding="0 16px"
        >
          <List
            sx={{
              display: "flex",
              flexDirection: "row",
              padding: 0,
              gap: 2,
            }}
          >
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
                <ListItem>
                  <NavItem href={"/stream-preview"}>Watch live</NavItem>
                </ListItem>
              </>
            )}
          </List>

          <List
            sx={{
              display: "flex",
              flexDirection: "row",
              padding: 0,
              gap: 2,
            }}
          >
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
          </List>
        </Box>
      )}
    </Paper>
  );
}
