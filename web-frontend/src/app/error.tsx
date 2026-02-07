'use client' // Error boundaries must be Client Components
 
import { HttpError } from '@/api/utils'
import { Button } from '@/components/ui/button'
import { Card, CardFooter } from '@/components/ui/card'
import { useToast } from '@/hooks/use-toast'
import { getAuthData } from '@/lib/auth/client'
import { logoutAction } from '@/lib/auth/server'
import { useRouter } from 'next/router'
import { Separator } from 'radix-ui'
import { useEffect } from 'react'
 
export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  const router = useRouter();
  const toast = useToast();
  useEffect(() => {
    console.error(error)
  }, [error])
  if (error instanceof HttpError && error.status === 403) {
    logoutAction().then(() => {
      router.push("/");
      toast.toast({
        title: "You have been logged out!",
        description: error.message
      })
    });

  }
  return (
    <Card className='pb-50 flex flex-column justify-evenly align-items-center'>
    <div className='flex justify-evenly align-center'>
      <h2 className='font-bold'>Something went wrong!</h2>
      <Separator.Root className="SeparatorRoot" orientation='vertical' />
      <p>Cannot access page - {error.message}</p>
    </div>
        
            <Button
            className='w-100 center'
            onClick={
                () => reset()
            }
            >
            Try again
            </Button>
      </Card>
  )
}