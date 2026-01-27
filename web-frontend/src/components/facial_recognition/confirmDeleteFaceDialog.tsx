import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogCancel,
  AlertDialogAction,
} from "@/components/ui/alert-dialog"

type Face = {
  id: number
  name: string
}

type ConfirmDeleteFaceDialogProps = {
  open: boolean
  face: Face | null
  onConfirm: () => void
  onCancel: () => void
}

export function ConfirmDeleteFaceDialog({
  open,
  face,
  onConfirm,
  onCancel,
}: ConfirmDeleteFaceDialogProps) {
  if (!face) return null

  return (
    <AlertDialog open={open} onOpenChange={(isOpen) => !isOpen && onCancel()}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            Delete face?
          </AlertDialogTitle>

          <AlertDialogDescription>
            This will permanently remove <strong>{face.name}</strong>.
            This action cannot be undone.
          </AlertDialogDescription>
        </AlertDialogHeader>

        <AlertDialogFooter>
          <AlertDialogCancel>
            Cancel
          </AlertDialogCancel>

          <AlertDialogAction
            onClick={onConfirm}
          >
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
