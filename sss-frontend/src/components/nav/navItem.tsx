import { Link, useLocation } from "react-router-dom";
import { FC, ReactNode } from "react";
import { Button } from "@mui/material";
interface INavItemProps {
  href: string;
  className?: string;
  children: string | ReactNode | JSX.Element;
  onClick?: () => void;
}

const NavItem: FC<INavItemProps> = function ({ href, children, onClick }) {
  const path = useLocation();
  const isActive = path.pathname === href;
  return (
    <Link to={href} onClick={onClick}>
      <Button variant={isActive ? "contained" : "outlined"}>{children}</Button>
    </Link>
  );
};
export default NavItem;
