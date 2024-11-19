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
  const { loggedIn, email, role, logout } = useContext(AuthContext);
  const [isDrawerOpen, setDrawerOpen] = useState(false);
  const isSmallScreen = useMediaQuery((theme: Theme) =>
      theme.breakpoints.down("md")
  );

  // Render items for the navigation
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
            <ListItem>
              <NavItem href={"/devices"}>Devices</NavItem>
            </ListItem>
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
              <NavItem href={"/login"}>
                <Box display="flex" alignItems="center" gap={1}>
                  <Person fontSize="small" />
                  Login
                </Box>
              </NavItem>
            </ListItem>
        )}
      </>
  );

  return (
      <Paper elevation={1} sx={{ marginTop: 0 }}>
        {isSmallScreen ? (
            // For small screens: Use Drawer
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
            // For large screens: Use horizontal navigation
            <Box
                display="flex"
                justifyContent="space-between"
                alignItems="center"
                padding="0 16px"
            >
              {/* Left side: Links */}
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
                    <ListItem>
                      <NavItem href={"/devices"}>Devices</NavItem>
                    </ListItem>
                )}
              </List>

              {/* Right side: Login/Logout */}
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
                      <NavItem href={"/login"}>
                        <Box
                            component="span"
                            display="flex"
                            alignItems="center"
                            justifyContent="center"
                            gap={1}
                        >
                          <Person fontSize="small" />
                          Login
                        </Box>
                      </NavItem>
                    </ListItem>
                )}
              </List>
            </Box>
        )}
      </Paper>
  );
}
